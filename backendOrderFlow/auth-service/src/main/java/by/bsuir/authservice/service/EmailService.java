package by.bsuir.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;





@Service
@Slf4j
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.from:noreply@orderflow.by}")
	private String fromAddress;

	@Value("${app.name:OrderFlow}")
	private String appName;

	@Autowired
	public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}




	@Async
	public void sendVerificationApprovedEmail(String toEmail, String companyName) {
		String subject = appName + " — Регистрация одобрена";
		String body = String.format(
				"Уважаемый пользователь!\n\n" +
				"Ваша заявка на регистрацию компании «%s» на платформе %s одобрена.\n" +
				"Теперь вы можете полноценно пользоваться всеми возможностями платформы.\n\n" +
				"Добро пожаловать!\n\n" +
				"С уважением,\nКоманда %s",
				companyName, appName, appName
		);
		sendEmail(toEmail, subject, body);
	}




	@Async
	public void sendVerificationRejectedEmail(String toEmail, String companyName, String reason) {
		String subject = appName + " — Регистрация отклонена";
		String body = String.format(
				"Уважаемый пользователь!\n\n" +
				"К сожалению, ваша заявка на регистрацию компании «%s» на платформе %s была отклонена.\n\n" +
				"Причина: %s\n\n" +
				"Вы можете исправить указанные замечания и подать заявку повторно.\n\n" +
				"С уважением,\nКоманда %s",
				companyName, appName, reason, appName
		);
		sendEmail(toEmail, subject, body);
	}




	@Async
	public void sendUserBlockedEmail(String toEmail, String reason) {
		String subject = appName + " — Учётная запись заблокирована";
		String body = String.format(
				"Уважаемый пользователь!\n\n" +
				"Ваша учётная запись на платформе %s была заблокирована.\n" +
				"Причина: %s\n\n" +
				"Для разблокировки обратитесь в службу поддержки.\n\n" +
				"С уважением,\nКоманда %s",
				appName, reason != null ? reason : "нарушение правил платформы", appName
		);
		sendEmail(toEmail, subject, body);
	}




	@Async
	public void sendUserUnblockedEmail(String toEmail) {
		String subject = appName + " — Учётная запись разблокирована";
		String body = String.format(
				"Уважаемый пользователь!\n\n" +
				"Ваша учётная запись на платформе %s была разблокирована.\n" +
				"Теперь вы снова можете пользоваться платформой.\n\n" +
				"С уважением,\nКоманда %s",
				appName, appName
		);
		sendEmail(toEmail, subject, body);
	}

	private void sendEmail(String to, String subject, String body) {
		if (mailSender == null) {
			log.warn("JavaMailSender not configured, skipping email to {}: {}", to, subject);
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromAddress);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
			log.info("Email sent to {}: {}", to, subject);
		} catch (MailException e) {
			log.error("Failed to send email to {}: {}", to, e.getMessage());
		}
	}
}
