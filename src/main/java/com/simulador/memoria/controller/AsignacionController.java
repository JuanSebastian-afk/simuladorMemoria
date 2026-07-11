package com.simulador.memoria.controller;

import com.simulador.memoria.model.asignacion.BloqueMemoria;
import com.simulador.memoria.model.asignacion.GestorMemoria;
import com.simulador.memoria.model.asignacion.Proceso;
import com.simulador.memoria.model.asignacion.ResultadoAsignacion;
import com.simulador.memoria.model.asignacion.TipoEstrategia;
import com.simulador.memoria.util.ConfiguracionEntrada;
import com.simulador.memoria.util.LectorEntrada;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Map;

/**
 * Controlador de la vista de Asignación de Memoria (First/Best/Worst Fit).
 */
public class AsignacionController {

    @FXML private Spinner<Integer> spinnerMemoriaTotal;
    @FXML private ComboBox<TipoEstrategia> comboEstrategia;
    @FXML private TextField campoIdProceso;
    @FXML private Spinner<Integer> spinnerTamanoProceso;
    @FXML private ComboBox<String> comboLiberar;
    @FXML private Label lblFragmentacionExterna;
    @FXML private Label lblFragmentacionInterna;
    @FXML private Label lblMemoriaLibre;
    @FXML private Label lblMemoriaOcupada;
    @FXML private HBox contenedorMemoria;
    @FXML private ScrollPane scrollMemoria;
    @FXML private ListView<String> listaProcesos;
    @FXML private TextArea areaLog;

    private GestorMemoria gestor;

    @FXML
    public void initialize() {
        spinnerMemoriaTotal.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(256, 8192, 1024, 64));
        spinnerTamanoProceso.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4096, 100, 1));
        comboEstrategia.getItems().addAll(TipoEstrategia.values());
        comboEstrategia.setValue(TipoEstrategia.FIRST_FIT);

        gestor = new GestorMemoria(1024);
        actualizarVista();
    }

    @FXML
    private void onInicializar() {
        int memoria = spinnerMemoriaTotal.getValue();
        gestor = new GestorMemoria(memoria, comboEstrategia.getValue());
        agregarLog("Memoria reiniciada: " + memoria + " KB con estrategia " + comboEstrategia.getValue().getNombre());
        actualizarVista();
    }

    @FXML
    private void onCambiarEstrategia() {
        if (gestor != null && comboEstrategia.getValue() != null) {
            gestor.setEstrategia(comboEstrategia.getValue());
            agregarLog("Estrategia cambiada a: " + comboEstrategia.getValue().getNombre());
        }
    }

    @FXML
    private void onAsignar() {
        String id = campoIdProceso.getText().trim();
        if (id.isEmpty()) {
            mostrarAlerta("Ingrese un ID de proceso.");
            return;
        }

        ResultadoAsignacion resultado = gestor.asignar(id, spinnerTamanoProceso.getValue());
        agregarLog(resultado.getMensaje());

        if (resultado.isExito()) {
            campoIdProceso.clear();
        }
        actualizarVista();
    }

    @FXML
    private void onLiberar() {
        String id = comboLiberar.getValue();
        if (id == null || id.isEmpty()) {
            mostrarAlerta("Seleccione un proceso para liberar.");
            return;
        }

        ResultadoAsignacion resultado = gestor.liberar(id);
        agregarLog(resultado.getMensaje());
        actualizarVista();
    }

    @FXML
    private void onCargarArchivo() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Cargar archivo de entrada");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de entrada", "*.json", "*.txt"),
                new FileChooser.ExtensionFilter("Todos", "*.*")
        );

        File archivo = chooser.showOpenDialog(contenedorMemoria.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try {
            ConfiguracionEntrada config = new LectorEntrada().leer(archivo.toPath());
            if (config.esPaginacion()) {
                mostrarAlerta("Este archivo es de paginación. Cárguelo en la pestaña correspondiente.");
                return;
            }

            spinnerMemoriaTotal.getValueFactory().setValue(config.getMemoriaTotal());
            comboEstrategia.setValue(TipoEstrategia.desdeTexto(config.getEstrategia()));
            gestor = new GestorMemoria(config.getMemoriaTotal(), comboEstrategia.getValue());

            for (ConfiguracionEntrada.ProcesoAsignacion p : config.getProcesosAsignacion()) {
                ResultadoAsignacion r = gestor.asignar(p.getId(), p.getTamano());
                agregarLog("[Archivo] " + r.getMensaje());
            }

            for (ConfiguracionEntrada.Operacion op : config.getOperaciones()) {
                ResultadoAsignacion r;
                if ("LIBERAR".equalsIgnoreCase(op.getAccion())) {
                    r = gestor.liberar(op.getProcesoId());
                } else {
                    r = gestor.asignar(op.getProcesoId(), op.getTamano());
                }
                agregarLog("[Archivo] " + r.getMensaje());
            }

            agregarLog("Archivo cargado: " + archivo.getName());
            actualizarVista();
        } catch (Exception e) {
            mostrarAlerta("Error al leer archivo: " + e.getMessage());
        }
    }

    private void actualizarVista() {
        renderizarMemoria();
        actualizarMetricas();
        actualizarListaProcesos();
    }

    private void renderizarMemoria() {
        contenedorMemoria.getChildren().clear();
        int memoriaTotal = gestor.getMemoriaTotal();
        double anchoDisponible = Math.max(scrollMemoria.getWidth() - 40, 800);

        for (BloqueMemoria bloque : gestor.getBloques()) {
            double proporcion = (double) bloque.getTamano() / memoriaTotal;
            double ancho = Math.max(proporcion * anchoDisponible, 60);

            VBox bloqueVisual = new VBox(4);
            bloqueVisual.setAlignment(Pos.CENTER);
            bloqueVisual.setPrefWidth(ancho);
            bloqueVisual.setMinWidth(ancho);
            bloqueVisual.setPrefHeight(100);
            bloqueVisual.getStyleClass().add(bloque.isLibre() ? "bloque-libre" : "bloque-ocupado");

            String titulo = bloque.isLibre() ? "LIBRE" : bloque.getIdProceso();
            Label lblTitulo = new Label(titulo);
            lblTitulo.getStyleClass().add("bloque-titulo");

            Label lblTamano = new Label(bloque.getTamano() + " KB");
            lblTamano.getStyleClass().add("bloque-detalle");

            Label lblDir = new Label("[" + bloque.getDireccionInicio() + "-" + bloque.getDireccionFin() + "]");
            lblDir.getStyleClass().add("bloque-detalle");

            bloqueVisual.getChildren().addAll(lblTitulo, lblTamano, lblDir);

            if (!bloque.isLibre() && bloque.getFragmentacionInterna() > 0) {
                Label lblFrag = new Label("FI: " + bloque.getFragmentacionInterna() + " KB");
                lblFrag.setTextFill(Color.web("#fbbf24"));
                bloqueVisual.getChildren().add(lblFrag);
            }

            HBox.setHgrow(bloqueVisual, Priority.NEVER);
            contenedorMemoria.getChildren().add(bloqueVisual);
        }

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
        contenedorMemoria.getChildren().add(espaciador);
    }

    private void actualizarMetricas() {
        lblFragmentacionExterna.setText(String.format("%.2f %%", gestor.calcularFragmentacionExterna()));
        lblFragmentacionInterna.setText(String.format("%d KB (%.2f %%)",
                gestor.calcularFragmentacionInternaTotal(),
                gestor.calcularPorcentajeFragmentacionInterna()));
        lblMemoriaLibre.setText(gestor.getMemoriaLibre() + " KB");
        lblMemoriaOcupada.setText(gestor.getMemoriaOcupada() + " KB");
    }

    private void actualizarListaProcesos() {
        listaProcesos.getItems().clear();
        comboLiberar.getItems().clear();

        for (Map.Entry<String, Proceso> entry : gestor.getProcesos().entrySet()) {
            Proceso p = entry.getValue();
            String info = String.format("%s | %d KB @ dir. %d", p.getId(), p.getTamanoSolicitado(), p.getDireccionAsignada());
            listaProcesos.getItems().add(info);
            comboLiberar.getItems().add(p.getId());
        }
    }

    private void agregarLog(String mensaje) {
        areaLog.appendText(mensaje + "\n");
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
