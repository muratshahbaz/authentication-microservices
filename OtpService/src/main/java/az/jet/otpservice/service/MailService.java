package az.jet.otpservice.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    public void send(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your OTP Verification Code");
            message.setText("Your OTP code is: " + code + "\n\nThis code expires in 5 minutes.");

            mailSender.send(message);
            log.info("✅ Email sent to: {}", email);

        } catch (Exception e) {
            log.error("❌ Failed to send email: {}", e.getMessage());
            // Для учебного проекта просто логируем
            log.info("📧 OTP for {} is: {}", email, code);
        }
    }
}