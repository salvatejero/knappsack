package com.sparc.knappsack.components.services.reports;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.models.reports.DirectedGraph;
import com.sparc.knappsack.models.reports.Link;
import com.sparc.knappsack.models.reports.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Transactional
@Service("reportService")
public class ReportServiceImpl implements ReportService {

    private final Random generator = new Random();

    @Autowired(required = true)
    private UserService userService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    public DirectedGraph createGraphForOrganization(Long organizationId) {
        DirectedGraph data = new DirectedGraph();

        List<Node> nodes = new ArrayList<Node>();
        List<Link> links = new ArrayList<Link>();
        Organization organization = organizationService.get(organizationId);

        Node rootNode = new Node();
        rootNode.setType("organization");
        rootNode.setId(Long.toString(organizationId));
        rootNode.setUuid(organization.getUuid());
        rootNode.setMatch(Float.toString(generator.nextFloat() * 1.0f));
        rootNode.setName(organization.getName());
        rootNode.setPopularity(Long.toString(organization.getUserDomains().size()));
        nodes.add(rootNode);

        addGroupNodes(nodes, links, organization);

        data.setNodes(nodes);
        data.setLinks(links);

        return data;
    }

    public DirectedGraph createGraphForAllAdministeredOrganizations() {
        DirectedGraph data = new DirectedGraph();

        List<Node> nodes = new ArrayList<Node>();
        List<Link> links = new ArrayList<Link>();
        List<Organization> organizations = userService.getAdministeredOrganizations(userService.getUserFromSecurityContext(), SortOrder.ASCENDING);

        Node rootNode = new Node();
        rootNode.setType("root");
        rootNode.setId("0");
        rootNode.setUuid(UUID.randomUUID().toString());
        rootNode.setMatch(Float.toString(generator.nextFloat() * 1.0f));
        rootNode.setName("Root Node");
        rootNode.setPopularity(Long.toString(organizations.size()));
        nodes.add(rootNode);

        addOrganizationNodes(rootNode, nodes, links, organizations);

        data.setNodes(nodes);
        data.setLinks(links);

        return data;
    }

    private void addOrganizationNodes(Node rootNode, List<Node> nodes, List<Link> links, List<Organization> organizations) {
        for (Organization organization : organizations) {
            Node node = new Node();
            node.setType("organization");
            node.setId(Long.toString(organization.getId()));
            node.setUuid(organization.getUuid());
            node.setMatch(Float.toString(generator.nextFloat() * 1.0f));
            node.setName(organization.getName());
            node.setPopularity(Long.toString(organization.getGroups().size()));
            nodes.add(node);

            addGroupNodes(nodes, links, organization);

            Link link = new Link();
            link.setSource(rootNode.getUuid());
            link.setTarget(organization.getUuid());
            links.add(link);
        }
    }

    private void addGroupNodes(List<Node> nodes, List<Link> links, Organization organization) {
        List<Group> groups = organization.getGroups();
        for (Group group : groups) {
            Node groupNode = new Node();
            groupNode.setType("group");
            groupNode.setId(Long.toString(group.getId()));
            groupNode.setUuid(group.getUuid());
            groupNode.setMatch(Float.toString(generator.nextFloat() * 1.0f));
            groupNode.setName(group.getName());
            groupNode.setPopularity(Long.toString(group.getOwnedApplications().size()));
            nodes.add(groupNode);

            addApplicationNodes(nodes, links, group);

            Link link = new Link();
            link.setSource(organization.getUuid());
            link.setTarget(group.getUuid());
            links.add(link);
        }
    }

    private void addApplicationNodes(List<Node> nodes, List<Link> links, Group group) {
        List<Application> applications = group.getOwnedApplications();
        for (Application application : applications) {
            Node appNode = new Node();
            appNode.setType("application");
            appNode.setId(Long.toString(application.getId()));
            appNode.setUuid(application.getUuid());
            appNode.setMatch(Float.toString(generator.nextFloat() * 1.0f));
            appNode.setName(application.getName());
            appNode.setPopularity(Long.toString(application.getApplicationVersions().size()));
            nodes.add(appNode);

            addApplicationVersionNodes(nodes, links, application);

            Link link = new Link();
            link.setSource(application.getUuid());
            link.setTarget(group.getUuid());
            links.add(link);
        }
    }

    private void addApplicationVersionNodes(List<Node> nodes, List<Link> links, Application application) {
        List<ApplicationVersion> appVersions = application.getApplicationVersions();
        for (ApplicationVersion appVersion : appVersions) {
            Node appVersionNode = new Node();
            appVersionNode.setType("applicationVersion");
            appVersionNode.setId(Long.toString(appVersion.getId()));
            appVersionNode.setUuid(appVersion.getUuid());
            appVersionNode.setMatch(Float.toString(generator.nextFloat() * 1.0f));
            appVersionNode.setName(appVersion.getVersionName());
            appVersionNode.setPopularity(Integer.toString(generator.nextInt() * 10));
            nodes.add(appVersionNode);

            Link link = new Link();
            link.setSource(appVersion.getUuid());
            link.setTarget(application.getUuid());
            links.add(link);

            Link link2 = new Link();
            link2.setSource(application.getUuid());
            link2.setTarget(appVersion.getUuid());
            links.add(link2);
        }
    }

}
