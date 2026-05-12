package com.trainticket;

import com.trainticket.cli.CommandLineInterface;
import com.trainticket.model.*;
import com.trainticket.repository.*;
import com.trainticket.service.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class TrainTicketingApplication {

    public static void main(String[] args) {
        // Initialize repositories
        StationRepository stationRepository = new StationRepository();
        RouteRepository routeRepository = new RouteRepository();
        TrainRepository trainRepository = new TrainRepository();
        ScheduleRepository scheduleRepository = new ScheduleRepository();
        BookingRepository bookingRepository = new BookingRepository();

        // Initialize services
        EmailService emailService = new EmailService(); // Demo mode - prints to console
        BookingService bookingService = new BookingService(bookingRepository, emailService);
        RouteFinderService routeFinderService = new RouteFinderService(scheduleRepository);
        AdminService adminService = new AdminService(routeRepository, trainRepository,
                scheduleRepository, bookingRepository, stationRepository, emailService);

        // Load sample data
        loadSampleData(stationRepository, routeRepository, trainRepository, scheduleRepository);

        // Start CLI
        CommandLineInterface cli = new CommandLineInterface(
                bookingService, routeFinderService, adminService,
                stationRepository, scheduleRepository, trainRepository, routeRepository);

        cli.start();
    }

    private static void loadSampleData(StationRepository stationRepo, RouteRepository routeRepo,
                                       TrainRepository trainRepo, ScheduleRepository scheduleRepo) {

        // Create stations
        Station london = new Station("LON", "London Paddington", "London");
        Station reading = new Station("RDG", "Reading", "Reading");
        Station swindon = new Station("SWN", "Swindon", "Swindon");
        Station bristol = new Station("BRI", "Bristol Temple Meads", "Bristol");
        Station cardiff = new Station("CDF", "Cardiff Central", "Cardiff");
        Station birmingham = new Station("BHM", "Birmingham New Street", "Birmingham");
        Station manchester = new Station("MAN", "Manchester Piccadilly", "Manchester");
        Station leeds = new Station("LDS", "Leeds", "Leeds");
        Station edinburgh = new Station("EDI", "Edinburgh Waverley", "Edinburgh");
        Station oxford = new Station("OXF", "Oxford", "Oxford");

        stationRepo.save(london);
        stationRepo.save(reading);
        stationRepo.save(swindon);
        stationRepo.save(bristol);
        stationRepo.save(cardiff);
        stationRepo.save(birmingham);
        stationRepo.save(manchester);
        stationRepo.save(leeds);
        stationRepo.save(edinburgh);
        stationRepo.save(oxford);

        // Create routes
        Route londonBristol = new Route("R001", "London to Bristol Express");
        londonBristol.addStop(london, 0, 0);
        londonBristol.addStop(reading, 25, 27);
        londonBristol.addStop(swindon, 50, 52);
        londonBristol.addStop(bristol, 90, 90);
        routeRepo.save(londonBristol);

        Route londonCardiff = new Route("R002", "London to Cardiff");
        londonCardiff.addStop(london, 0, 0);
        londonCardiff.addStop(reading, 25, 27);
        londonCardiff.addStop(swindon, 50, 52);
        londonCardiff.addStop(bristol, 90, 95);
        londonCardiff.addStop(cardiff, 140, 140);
        routeRepo.save(londonCardiff);

        Route londonManchester = new Route("R003", "London to Manchester");
        londonManchester.addStop(london, 0, 0);
        londonManchester.addStop(birmingham, 85, 90);
        londonManchester.addStop(manchester, 150, 150);
        routeRepo.save(londonManchester);

        Route manchesterEdinburgh = new Route("R004", "Manchester to Edinburgh");
        manchesterEdinburgh.addStop(manchester, 0, 0);
        manchesterEdinburgh.addStop(leeds, 50, 55);
        manchesterEdinburgh.addStop(edinburgh, 180, 180);
        routeRepo.save(manchesterEdinburgh);

        Route bristolBirmingham = new Route("R005", "Bristol to Birmingham");
        bristolBirmingham.addStop(bristol, 0, 0);
        bristolBirmingham.addStop(birmingham, 70, 70);
        routeRepo.save(bristolBirmingham);

        // Create trains
        Train express1 = new Train("T001", "Intercity Express 1", 300, Train.TrainType.EXPRESS);
        Train express2 = new Train("T002", "Intercity Express 2", 300, Train.TrainType.EXPRESS);
        Train highSpeed1 = new Train("T003", "High Speed 1", 400, Train.TrainType.HIGH_SPEED);
        Train regional1 = new Train("T004", "Regional Service 1", 150, Train.TrainType.REGIONAL);
        Train intercity1 = new Train("T005", "Intercity 1", 250, Train.TrainType.INTERCITY);

        trainRepo.save(express1);
        trainRepo.save(express2);
        trainRepo.save(highSpeed1);
        trainRepo.save(regional1);
        trainRepo.save(intercity1);

        // Create schedules for today
        LocalDate today = LocalDate.now();

        scheduleRepo.save(new TrainSchedule("S001", express1, londonBristol, LocalTime.of(8, 0), today));
        scheduleRepo.save(new TrainSchedule("S002", express2, londonBristol, LocalTime.of(10, 0), today));
        scheduleRepo.save(new TrainSchedule("S003", highSpeed1, londonCardiff, LocalTime.of(9, 0), today));
        scheduleRepo.save(new TrainSchedule("S004", intercity1, londonManchester, LocalTime.of(8, 30), today));
        scheduleRepo.save(new TrainSchedule("S005", regional1, manchesterEdinburgh, LocalTime.of(12, 0), today));
        scheduleRepo.save(new TrainSchedule("S006", express1, bristolBirmingham, LocalTime.of(14, 0), today));

        // Add schedules for tomorrow
        LocalDate tomorrow = today.plusDays(1);
        scheduleRepo.save(new TrainSchedule("S007", express1, londonBristol, LocalTime.of(8, 0), tomorrow));
        scheduleRepo.save(new TrainSchedule("S008", highSpeed1, londonCardiff, LocalTime.of(9, 0), tomorrow));
        scheduleRepo.save(new TrainSchedule("S009", intercity1, londonManchester, LocalTime.of(8, 30), tomorrow));

        System.out.println("Sample data loaded successfully.");
        System.out.println("Stations: " + stationRepo.findAll().size());
        System.out.println("Routes: " + routeRepo.findAll().size());
        System.out.println("Trains: " + trainRepo.findAll().size());
        System.out.println("Schedules: " + scheduleRepo.findAll().size());
    }
}
