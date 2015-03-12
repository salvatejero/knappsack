package com.sparc.knappsack.components.services;

import com.sparc.knappsack.util.WebRequest;

public interface IOSService {

    String createIOSPlistXML(Long applicationVersionId, WebRequest webRequest, String token);

}
