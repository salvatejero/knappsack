package com.sparc.knappsack.components.services.reports;

import com.sparc.knappsack.models.reports.DirectedGraph;

public interface ReportService {
    DirectedGraph createGraphForAllAdministeredOrganizations();

    DirectedGraph createGraphForOrganization(Long organizationId);
}
