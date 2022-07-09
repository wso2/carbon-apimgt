package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CertificateValidityDTO;

/**
 * This class is responsible for converting between certificate objects.
 */

public class CertificateMappingUtil {

    /**
     * To convert Instance of {@link CertificateInformationDTO} to {@link CertificateInfoDTO};
     *
     * @param certificateInformationDTO Instance of {@link CertificateInformationDTO}
     * @return converted instance of {@link CertificateInfoDTO}.
     */

    public static CertificateInfoDTO fromCertificateInformationToDTO(
            CertificateInformationDTO certificateInformationDTO) {
        CertificateValidityDTO certificateValidityDTO = new CertificateValidityDTO();
        certificateValidityDTO.setFrom(certificateInformationDTO.getFrom());
        certificateValidityDTO.setTo(certificateInformationDTO.getTo());

        CertificateInfoDTO certificateInfoDTO = new CertificateInfoDTO();
        certificateInfoDTO.setValidity(certificateValidityDTO);
        certificateInfoDTO.setStatus(certificateInformationDTO.getStatus());
        certificateInfoDTO.setSubject(certificateInformationDTO.getSubject());
        certificateInfoDTO.setVersion(certificateInformationDTO.getVersion());
        certificateInfoDTO.setSerialNumber(certificateInformationDTO.getSerialNumber());
        return certificateInfoDTO;
    }
}