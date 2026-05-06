module com.example.oop2_capstone {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;

    opens com.example.oop2_capstone to javafx.fxml;
    exports com.example.oop2_capstone;
}