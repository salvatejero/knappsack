package com.sparc.knappsack.components.services;

import java.io.OutputStream;
import java.util.Date;

public interface ExcelService {

    void createOrganizationDetailsForDateRangeReport(Date minDate, Date maxDate, OutputStream outputStream);

}
