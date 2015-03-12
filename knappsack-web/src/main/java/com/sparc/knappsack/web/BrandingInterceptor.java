package com.sparc.knappsack.web;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.CustomBranding;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.AppFileService;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.components.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BrandingInterceptor extends FilteredRequestHandlerInterceptorAdapter {

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("appFileService")
    @Autowired(required = true)
    private AppFileService appFileService;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (request != null && modelAndView != null && (!(modelAndView.getView() instanceof RedirectView) && !modelAndView.getViewName().startsWith("redirect:"))) {
            if(isBlackLabeledRequest(request)) {
                return;
            }

            User user = userService.getUserFromSecurityContext();
            if (user != null) {
                Organization organization = user.getActiveOrganization();

                if (organization != null) {
                    modelAndView.getModel().put("orgName", organization.getName());
                    if (organizationService.isCustomBrandingEnabled(organization)) {
                        modelAndView.getModel().put("orgName", organization.getName());
                        CustomBranding customBranding = organization.getCustomBranding();
                        if (customBranding != null) {

                            // Add logo URL to model is exists
                            AppFile logo = customBranding.getLogo();
                            if (logo != null) {
                                modelAndView.getModel().put("customLogoURL", appFileService.getImageUrl(logo));
                                modelAndView.getModel().put("customLogoOrganizationName", StringUtils.trimTrailingWhitespace(organization.getName()));
                            }

                        }
                    }
                }
            }
        }
    }
}
