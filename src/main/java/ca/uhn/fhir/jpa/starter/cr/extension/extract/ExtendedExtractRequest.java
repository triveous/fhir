package ca.uhn.fhir.jpa.starter.cr.extension.extract;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.opencds.cqf.cql.engine.model.ModelResolver;
import org.opencds.cqf.fhir.cql.LibraryEngine;
import org.opencds.cqf.fhir.cr.questionnaireresponse.extract.ExtractRequest;

public class ExtendedExtractRequest extends ExtractRequest {
	private IBaseResource structureMap;

	public ExtendedExtractRequest(IBaseResource questionnaireResponse, IBaseResource questionnaire, IIdType subjectId, IBaseParameters parameters, IBaseBundle bundle, boolean useServerData,LibraryEngine libraryEngine, ModelResolver modelResolver) {
		super(questionnaireResponse, questionnaire, subjectId, parameters, bundle,useServerData, libraryEngine, modelResolver);
	}

	public IBaseResource getStructureMap() {
		return structureMap;
	}

	public void setStructureMap(IBaseResource structureMap) {
		this.structureMap = structureMap;
	}
}
