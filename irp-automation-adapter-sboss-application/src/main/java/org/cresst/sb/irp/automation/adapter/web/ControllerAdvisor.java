package org.cresst.sb.irp.automation.adapter.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice(assignableTypes = AdapterController.class)
public class ControllerAdvisor {

    private final static Logger logger = LoggerFactory.getLogger(ControllerAdvisor.class);

    @ModelAttribute
    public void addAttributes(HttpServletRequest request, HttpServletResponse response, Model model, @RequestBody String requestString) {
        logger.info("requestString: " + requestString);
    }

}