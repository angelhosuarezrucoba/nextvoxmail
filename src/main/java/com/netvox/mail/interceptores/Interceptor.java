/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.interceptores;

import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

/**
 *
 * @author desarrollo5
 */
public class Interceptor extends HttpSessionHandshakeInterceptor {

    private static final UriTemplate URI_TEMPLATE = new UriTemplate("/mail/{idagente}");

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes)
            throws Exception {
        Map<String, String> mapauri = URI_TEMPLATE.match(request.getURI().getPath());
        attributes.put("idagente", mapauri.get("idagente"));
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

}
