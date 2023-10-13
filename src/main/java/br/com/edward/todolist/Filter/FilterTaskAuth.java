package br.com.edward.todolist.Filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.edward.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter) throws ServletException, IOException {

        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")){
            var basicAuthorization = request.getHeader("Authorization");
            System.out.println("Basic Authorization -> " + basicAuthorization);

            var authEncoded = basicAuthorization.substring("Basic".length()).trim();
            System.out.println("AuthEncoded -> " + authEncoded);

            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
            System.out.println("AuthEncoded -> " + authEncoded);

            var authString = new String(authDecoded);
            System.out.println("AuthString -> " + authString);

            String[] credentials = authString.split(":");
            String userName = credentials[0];
            String password = credentials[1];

            var user = this.userRepository.findByUserName(userName);
            if (user == null){
                response.sendError(401);
            } else {
                var passwordVerified = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if(passwordVerified.verified){
                    request.setAttribute("idUser", user.getId());
                    filter.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
            }
        } else {
            filter.doFilter(request, response);
        }
    }
}
