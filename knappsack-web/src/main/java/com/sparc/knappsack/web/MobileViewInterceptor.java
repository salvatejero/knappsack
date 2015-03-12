package com.sparc.knappsack.web;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.sparc.knappsack.util.UserAgentInfo;

public class MobileViewInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory
			.getLogger(MobileViewInterceptor.class);
	
    private String mobileDirectoryPrefix;
    private boolean mobileEnabled;
    private String viewsRootDirectory;
    private String viewSuffix;

    public void setMobileDirectoryPrefix(String mobileDirectoryPrefix) {
        this.mobileDirectoryPrefix = mobileDirectoryPrefix;
    }

    public String getMobileDirectoryPrefix() {
        return mobileDirectoryPrefix;
    }

    public void setMobileEnabled(boolean mobileEnabled) {
        this.mobileEnabled = mobileEnabled;
    }

    public boolean isMobileEnabled() {
        return mobileEnabled;
    }

    public void setViewsRootDirectory(String viewsRootDirectory) {
        this.viewsRootDirectory = viewsRootDirectory;
    }

    public String getViewsRootDirectory() {
        return viewsRootDirectory;
    }

    public void setViewSuffix(String viewSuffix) {
        this.viewSuffix = viewSuffix;
    }

    public String getViewSuffix() {
        return viewSuffix;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            if (isMobileEnabled() && checkUserAgentIsMobile(request)) {
                String mobileFileName = request.getSession(false).getServletContext().getRealPath(getViewsRootDirectory() + getMobileDirectoryPrefix() + modelAndView.getViewName() + getViewSuffix());
                if (checkIfResourceExists(mobileFileName)) {
                    modelAndView.setViewName(getMobileDirectoryPrefix() + modelAndView.getViewName());
                }
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {}

    private boolean checkIfResourceExists(String resourceFullName) {
        log.error(resourceFullName);
        try{
        	File reqFile = new File(resourceFullName);
        	return reqFile.exists();
        }catch(Exception e){
        	
        }
        return true;
    }

    private boolean checkUserAgentIsMobile(HttpServletRequest request) {
        UserAgentInfo uAgentInfo = new UserAgentInfo(request.getHeader("User-Agent"), request.getHeader("Accept"));

        return (uAgentInfo.detectMobileLong() || uAgentInfo.detectTierTablet());
    }



}
