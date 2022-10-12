package org.wso2.apk.apimgt.impl.importexport.utils;

import org.wso2.apk.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

public class APIImportExportUtil {

    public static ImportExportAPI getImportExportAPI() {

        return ServiceReferenceHolder.getInstance().getImportExportService();
    }

}
