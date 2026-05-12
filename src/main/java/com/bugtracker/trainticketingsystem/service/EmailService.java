package com.trainticket.service;

import com.trainticket.model.Booking;
import com.trainticket.model.Ticket;
import com.trainticket.model.TrainSchedule;
import com.trainticket.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final String smtpHost;
    private final int smtpPort;
    private final String username;
    private final String password;
    private final boolean enabled;

    public EmailService(String smtpHost, int smtpPort, String username, String password) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.enabled = smtpHost != null && !smtpHost.isEmpty();
    }

    // Constructor for testing/demo mode without actual email sending
    public EmailService() {
        this.smtpHost = null;
        this.smtpPort = 0;
        this.username = null;
        this.password = null;
        this.enabled = false;
    }

    public void sendBookingConfirmation(Booking booking) {
        String subject = "Booking Confirmation - " + booking.getId();
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(booking.getUser().getName()).append(",\n\n");
        body.append("Your booking has been confirmed!\n\n");
        body.append("Booking Reference: ").append(booking.getId()).append("\n");
        body.append("Booking Time: ").append(booking.getBookingTime()).append("\n\n");
        body.append("Tickets:\n");
        body.append("-".repeat(50)).append("\n");

        for (Ticket ticket : booking.getTickets()) {
            body.append(String.format("  Train: %s\n", ticket.getSchedule().getTrain().getName()));
            body.append(String.format("  From: %s at %s\n",
                    ticket.getFromStation().getName(),
                    ticket.getSchedule().getStationDepartureTime(ticket.getFromStation())));
            body.append(String.format("  To: %s at %s\n",
                    ticket.getToStation().getName(),
                    ticket.getSchedule().getStationArrivalTime(ticket.getToStation())));
            body.append(String.format("  Seat: %d\n", ticket.getSeatNumber()));
            body.append(String.format("  Price: $%.2f\n", ticket.getPrice()));
            body.append("-".repeat(50)).append("\n");
        }

        double total = booking.getTickets().stream().mapToDouble(Ticket::getPrice).sum();
        body.append(String.format("\nTotal: $%.2f\n", total));
        body.append("\nThank you for choosing our service!\n");

        sendEmail(booking.getUser().getEmail(), subject, body.toString());
    }

    public void sendDelayNotification(User user, TrainSchedule schedule) {
        String subject = "Train Delay Notification - " + schedule.getTrain().getName();
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(user.getName()).append(",\n\n");
        body.append("We regret to inform you that your train has been delayed.\n\n");
        body.append("Train: ").append(schedule.getTrain().getName()).append("\n");
        body.append("Route: ").append(schedule.getRoute().getName()).append("\n");
        body.append("Original Departure: ").append(schedule.getDepartureTime()).append("\n");
        body.append("Delay: ").append(schedule.getDelayMinutes()).append(" minutes\n");
        body.append("New Expected Departure: ").append(schedule.getActualDepartureDateTime().toLocalTime()).append("\n\n");
        body.append("We apologize for any inconvenience caused.\n");

        sendEmail(user.getEmail(), subject, body.toString());
    }

    private void sendEmail(String to, String subject, String body) {
        if (!enabled) {
            logger.info("Email service disabled. Would send email to: {}", to);
            logger.info("Subject: {}", subject);
            logger.info("Body:\n{}", body);
            System.out.println("\n" + "=".repeat(60));
            System.out.println("EMAIL NOTIFICATION (Demo Mode)");
            System.out.println("=".repeat(60));
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("-".repeat(60));
            System.out.println(body);
            System.out.println("=".repeat(60) + "\n");
            return;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to: {}", to, e);
        }
    }
}
