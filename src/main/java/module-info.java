module com.example.oop2_capstone {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires java.prefs;
    requires io.github.cdimascio.dotenv.java;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;

    opens com.example.oop2_capstone to javafx.fxml;
    opens models;
    opens pages.login;
    opens pages.dashboard;
    opens pages.ticket_board;
    opens pages.ticket_list;
    opens pages.report;
    opens pages.notification_popup;
    opens pages.admin_panel;
    opens pages.add_user;
    opens pages.create_ticket;
    opens pages.profile;

    exports com.example.oop2_capstone;
    exports models;
    exports pages.login;
    exports pages.dashboard;
    exports pages.ticket_board;
    exports pages.ticket_list;
    exports pages.report;
    exports pages.notification_popup;
    exports pages.admin_panel;
    exports pages.add_user;
    exports pages.create_ticket;
    exports pages.profile;
    exports workers;
    opens workers;
}
