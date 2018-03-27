package io.shardingjdbc.console.controller;

import io.shardingjdbc.console.constant.ResponseCode;
import io.shardingjdbc.console.entity.DBConnector;
import io.shardingjdbc.console.entity.SessionRegistry;
import io.shardingjdbc.console.entity.ResponseObject;
import io.shardingjdbc.console.entity.UserSession;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * User controller.
 * 
 * @author panjuan
 */
@RestController
@RequestMapping("/user")
public class UserController {
    
    /**
     * Handle https for user login.
     * 
     * @param userSession user info
     * @param userUUID id
     * @param response response
     * @return response object
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ResponseObject login(@RequestBody final UserSession userSession, final @CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID,
                                final HttpServletResponse response) {
        if (!"".equals(userUUID)) {
            return new ResponseObject(ResponseCode.SUCCESS);
        }
        Connection connection;
        try {
            connection = DBConnector.getConnection(userSession.getUserName(), userSession.getPassWord(), userSession.getTargetURL(), userSession.getDriver());
        } catch (final ClassNotFoundException | SQLException ex) {
            // TODO
            ex.getMessage();
            return new ResponseObject(ResponseCode.FAILURE);
        }
        setSession(userSession, response, connection);
        return new ResponseObject(ResponseCode.SUCCESS);
    }

    private void setSession(final UserSession userSession, final HttpServletResponse response, final Connection connection) {
        SessionRegistry.getInstance().addSession(userSession.getId(), connection);
        Cookie cookie = new Cookie("userUUID", userSession.getId());
        cookie.setMaxAge(120 * 60);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * Handle http for user exiting.
     * 
     * @param userUUID useruuid
     * @param response response
     * @return response object
     */
    @RequestMapping(value = "/exit", method = RequestMethod.POST)
    public ResponseObject exit(final @CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID, final HttpServletResponse response) {
        if (!"".equals(userUUID)) {
            removeSession(userUUID, response);
        }
        return new ResponseObject(ResponseCode.SUCCESS);
    }

    private void removeSession(@CookieValue(value = "userUUID", required = false, defaultValue = "") String userUUID, HttpServletResponse response) {
        SessionRegistry.getInstance().removeSession(userUUID);
        Cookie cookie = new Cookie("userUUID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
