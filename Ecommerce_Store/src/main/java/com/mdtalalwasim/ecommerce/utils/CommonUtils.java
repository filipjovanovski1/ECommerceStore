package com.mdtalalwasim.ecommerce.utils;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtils {

	public static String generateUrl(HttpServletRequest request) {
		String fullUrl = request.getRequestURL().toString();
		return fullUrl.replace(request.getServletPath(), "");
	}

	
}
