package com.gymplatform.web;

import com.gymplatform.security.CurrentAccountPrincipal;
import com.gymplatform.service.BusinessConflictException;
import java.util.NoSuchElementException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages="com.gymplatform.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebAdvice {
    @ModelAttribute void identity(Authentication auth, Model model) {
        model.addAttribute("admin", false); model.addAttribute("platform", false);
        if (auth != null && auth.getPrincipal() instanceof CurrentAccountPrincipal user) {
            model.addAttribute("email", user.email());
            model.addAttribute("admin", auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            model.addAttribute("platform", auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")));
            model.addAttribute("roleLabel", auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")) ? "Superadmin"
                    : auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "Administración" : "Recepción");
        }
    }
    @ExceptionHandler(NoSuchElementException.class) ModelAndView missing(Exception ex) { return error(404, "No encontramos este recurso", "El recurso no existe o no está disponible en tu gimnasio."); }
    @ExceptionHandler(AccessDeniedException.class) ModelAndView denied(Exception ex) { return error(403, "Acceso denegado", "Tu cuenta no tiene permiso para realizar esta acción."); }
    @ExceptionHandler({IllegalArgumentException.class, BindException.class, org.springframework.beans.TypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class})
    ModelAndView invalid(Exception ex) { return error(400, "Revisá los datos ingresados", "Completá los campos obligatorios. El DNI debe tener 7 u 8 dígitos y los importes deben ser positivos, con punto y dos decimales. Volvé al formulario para corregirlo."); }
    @ExceptionHandler({BusinessConflictException.class, org.springframework.dao.DataIntegrityViolationException.class,
            org.springframework.orm.ObjectOptimisticLockingFailureException.class})
    ModelAndView conflict(Exception ex) { return error(409, "La operación no se pudo completar", "Puede existir un registro con los mismos datos, una renovación previa o un cambio en el saldo. Revisá el registro antes de volver a intentarlo."); }
    @ExceptionHandler(Exception.class) ModelAndView unexpected(Exception ex) {
        org.slf4j.LoggerFactory.getLogger(WebAdvice.class).error("Web request failed ({})", ex.getClass().getSimpleName());
        return error(500, "No pudimos completar la solicitud", "Si estabas registrando una compra o un cobro, consultá el historial antes de repetir la operación.");
    }
    private ModelAndView error(int status, String title, String message) {
        var view = new ModelAndView("web/error"); view.setStatus(HttpStatus.valueOf(status));
        view.addObject("status",status); view.addObject("title",title); view.addObject("message",message); return view;
    }
}
