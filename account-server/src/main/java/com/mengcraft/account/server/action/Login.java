package com.mengcraft.account.server.action;

import com.mengcraft.account.server.MD5Util;
import com.mengcraft.account.server.ServerMain;
import com.mengcraft.account.server.SessionBuilder;
import com.mengcraft.account.server.SessionMap;
import com.mengcraft.account.server.entity.BeanUser;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import static com.avaje.ebean.Ebean.find;

/**
 * Created by on 16-4-14.
 */
@Path("/login/{name}/{secure}")
public class Login {

    public static final SessionMap SESSION_MAP = new SessionMap();

    @GET
    public void process(@BeanParam LoginRequest request, @Suspended AsyncResponse response) {
        ServerMain.POOL.execute(() -> {
            LoginResponse response1 = new LoginResponse();
            BeanUser user = find(BeanUser.class)
                    .where()
                    .eq("username", request.getName())
                    .findUnique();
            if (user != null) {
                MD5Util util = new MD5Util();
                try {
                    String digest = util.digest(request.getSecure() + user.getSalt());
                    if (user.getPassword().equals(digest)) {
                        accept(request, response1);
                    }
                } catch (Exception ignored) {
                }
            }
            response.resume(response1);
        });
    }

    private void accept(LoginRequest request, LoginResponse response) {
        String session = new SessionBuilder().nextSession();
        SESSION_MAP.put(request.getName(), session);
        response.setSession(session);
    }

}