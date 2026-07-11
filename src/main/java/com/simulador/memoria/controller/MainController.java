package com.simulador.memoria.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Objects;

/**
 * Controlador principal que carga las pestañas de cada módulo del simulador.
 */
public class MainController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Tab tabAsignacion;

    @FXML
    private Tab tabPaginacion;

    @FXML
    public void initialize() {
        try {
            FXMLLoader loaderAsignacion = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxml/asignacion.fxml")));
            tabAsignacion.setContent(loaderAsignacion.load());

            FXMLLoader loaderPaginacion = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxml/paginacion.fxml")));
            tabPaginacion.setContent(loaderPaginacion.load());
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar las vistas del simulador", e);
        }
    }
}
