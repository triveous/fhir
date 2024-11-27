package ca.uhn.fhir.jpa.starter.cr.extension.extract;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.*;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cql.EvaluationSettings;
import org.opencds.cqf.fhir.cql.LibraryEngine;
import org.opencds.cqf.fhir.cr.questionnaireresponse.QuestionnaireResponseProcessor;
import org.opencds.cqf.fhir.cr.questionnaireresponse.extract.IExtractProcessor;
import org.opencds.cqf.fhir.utility.SearchHelper;
import org.opencds.cqf.fhir.utility.monad.Either;

@SuppressWarnings("UnstableApiUsage")
public class ExtendedQuestionnaireResponseProcessor extends QuestionnaireResponseProcessor {
	private static final String SDC_TARGET_STRUCTURE_MAP = "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-targetStructureMap";

	public ExtendedQuestionnaireResponseProcessor(Repository repository) {
		this(repository, EvaluationSettings.getDefault());
	}

	public ExtendedQuestionnaireResponseProcessor(Repository repository, EvaluationSettings evaluationSettings) {
		this(repository, evaluationSettings, null);
	}

	public ExtendedQuestionnaireResponseProcessor(Repository repository, EvaluationSettings evaluationSettings, IExtractProcessor extractProcessor) {
		super(repository, evaluationSettings, extractProcessor);
		this.extractProcessor = extractProcessor != null ? extractProcessor : new ExtendedExtractProcessor();
	}

	protected void addStructureMap(ExtendedExtractRequest extractRequest) {
		var extension = extractRequest.getExtensionByUrl(extractRequest.getQuestionnaire(), SDC_TARGET_STRUCTURE_MAP);
		if (extension == null || StringUtils.isBlank(extension.getUrl())) return;
		IPrimitiveType<String> canonical = (IPrimitiveType<String>) extension.getValue();
		try {
			var structuremap = SearchHelper.searchRepositoryByCanonical(
				repository,
				canonical,
				repository
					.fhirContext()
					.getResourceDefinition("structuremap")
					.getImplementingClass());
			if (structuremap == null) return;
			extractRequest.setStructureMap(structuremap);
		} catch (FHIRException e) {
			logger.error(e.getMessage());
		}
	}


	@Override
	public <R extends IBaseResource> IBaseBundle extract(Either<IIdType, R> questionnaireResponseId, Either<IIdType, R> questionnaireId, IBaseParameters parameters, IBaseBundle data, boolean useServerData, LibraryEngine libraryEngine) {
		var questionnaireResponse = resolveQuestionnaireResponse(questionnaireResponseId);
		var questionnaire = resolveQuestionnaire(questionnaireResponse,questionnaireId);
		var subject = (IBaseReference) modelResolver.resolvePath(questionnaireResponse, "subject");
		var request = new ExtendedExtractRequest(
			questionnaireResponse,
			questionnaire,
			subject == null ? null : subject.getReferenceElement(),
			parameters,
			data,
			useServerData,
			libraryEngine,
			modelResolver);

		addStructureMap(request);
		return extractProcessor.extract(request);
	}
}
