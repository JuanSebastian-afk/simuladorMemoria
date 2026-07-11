package com.simulador.memoria.controller;

import com.simulador.memoria.model.paginacion.EntradaPagina;
import com.simulador.memoria.model.paginacion.GestorPaginacion;
import com.simulador.memoria.model.paginacion.Marco;
import com.simulador.memoria.model.paginacion.PasoTraduccion;
import com.simulador.memoria.model.paginacion.ResultadoTraduccion;
import com.simulador.memoria.model.paginacion.TablaPaginas;
import com.simulador.memoria.util.ConfiguracionEntrada;
import com.simulador.memoria.util.LectorEntrada;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controlador de la vista de Paginación y traducción de direcciones.
 */
public class PaginacionController {

    @FXML private Spinner<Integer> spinnerMemoriaFisica;
    @FXML private ComboBox<Integer> comboTamanoPagina;
    @FXML private TextField campoIdProceso;
    @FXML private Spinner<Integer> spinnerEspacioVirtual;
    @FXML private Spinner<Integer> spinnerPaginaCargar;
    @FXML private Spinner<Integer> spinnerMarcoAsignar;
    @FXML private ComboBox<String> comboProcesoTraducir;
    @FXML private TextField campoDireccionVirtual;
    @FXML private Label lblResultadoTraduccion;
    @FXML private TextArea areaPasos;
    @FXML private TableView<FilaTablaPaginas> tablaPaginas;
    @FXML private TableColumn<FilaTablaPaginas, Integer> colPagina;
    @FXML private TableColumn<FilaTablaPaginas, Integer> colMarco;
    @FXML private TableColumn<FilaTablaPaginas, String> colValida;
    @FXML private ListView<String> listaMarcos;
    @FXML private GridPane gridMarcos;
    @FXML private TextArea areaLog;

    private GestorPaginacion gestor;

    @FXML
    public void initialize() {
        spinnerMemoriaFisica.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(4096, 65536, 16384, 1024));
        comboTamanoPagina.getItems().addAll(1024, 2048, 4096, 8192);
        comboTamanoPagina.setValue(4096);
        spinnerEspacioVirtual.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(4096, 131072, 32768, 1024));
        spinnerPaginaCargar.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0, 1));
        spinnerMarcoAsignar.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 15, 0, 1));

        colPagina.setCellValueFactory(new PropertyValueFactory<>("pagina"));
        colMarco.setCellValueFactory(new PropertyValueFactory<>("marco"));
        colValida.setCellValueFactory(new PropertyValueFactory<>("valida"));

        inicializarGestor();
    }

    @FXML
    private void onInicializar() {
        inicializarGestor();
        agregarLog("Sistema de paginación reiniciado.");
        actualizarVista();
    }

    @FXML
    private void onRegistrarProceso() {
        String id = campoIdProceso.getText().trim();
        if (id.isEmpty()) {
            mostrarAlerta("Ingrese un ID de proceso.");
            return;
        }

        gestor.registrarProceso(id, spinnerEspacioVirtual.getValue());
        comboProcesoTraducir.getItems().add(id);
        comboProcesoTraducir.setValue(id);
        agregarLog("Proceso " + id + " registrado con espacio virtual de " + spinnerEspacioVirtual.getValue() + " bytes.");
        campoIdProceso.clear();
        actualizarVista();
    }

    @FXML
    private void onCargarPagina() {
        String id = comboProcesoTraducir.getValue();
        if (id == null) {
            mostrarAlerta("Seleccione o registre un proceso.");
            return;
        }

        boolean ok = gestor.cargarPagina(id, spinnerPaginaCargar.getValue(), spinnerMarcoAsignar.getValue());
        if (ok) {
            agregarLog(String.format("Página %d del proceso %s cargada en marco %d.",
                    spinnerPaginaCargar.getValue(), id, spinnerMarcoAsignar.getValue()));
        } else {
            agregarLog("Error al cargar la página. Verifique marco y número de página.");
        }
        actualizarVista();
    }

    @FXML
    private void onTraducir() {
        String id = comboProcesoTraducir.getValue();
        if (id == null) {
            mostrarAlerta("Seleccione un proceso.");
            return;
        }

        int dv;
        try {
            dv = Integer.parseInt(campoDireccionVirtual.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta("Ingrese una dirección virtual numérica válida.");
            return;
        }

        ResultadoTraduccion resultado = gestor.traducirDireccion(id, dv);
        mostrarResultadoTraduccion(resultado);
        agregarLog(resultado.getMensaje());
    }

    @FXML
    private void onCargarArchivo() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Cargar archivo de paginación");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de entrada", "*.json", "*.txt"),
                new FileChooser.ExtensionFilter("Todos", "*.*")
        );

        File archivo = chooser.showOpenDialog(gridMarcos.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try {
            ConfiguracionEntrada config = new LectorEntrada().leer(archivo.toPath());
            if (!config.esPaginacion()) {
                mostrarAlerta("Este archivo es de asignación. Cárguelo en la pestaña correspondiente.");
                return;
            }

            spinnerMemoriaFisica.getValueFactory().setValue(config.getMemoriaFisica());
            if (comboTamanoPagina.getItems().contains(config.getTamanoPagina())) {
                comboTamanoPagina.setValue(config.getTamanoPagina());
            } else {
                comboTamanoPagina.getItems().add(config.getTamanoPagina());
                comboTamanoPagina.setValue(config.getTamanoPagina());
            }

            gestor = new GestorPaginacion(config.getMemoriaFisica(), config.getTamanoPagina());
            comboProcesoTraducir.getItems().clear();

            for (ConfiguracionEntrada.ProcesoPaginacion p : config.getProcesosPaginacion()) {
                gestor.registrarProceso(p.getId(), p.getEspacioVirtual());
                comboProcesoTraducir.getItems().add(p.getId());
                for (ConfiguracionEntrada.PaginaCargada pc : p.getPaginasCargadas()) {
                    gestor.cargarPagina(p.getId(), pc.getPagina(), pc.getMarco());
                }
                agregarLog("[Archivo] Proceso " + p.getId() + " configurado.");
            }

            if (!config.getTraducciones().isEmpty()) {
                ConfiguracionEntrada.Traduccion t = config.getTraducciones().get(0);
                comboProcesoTraducir.setValue(t.getProcesoId());
                campoDireccionVirtual.setText(String.valueOf(t.getDireccionVirtual()));
                ResultadoTraduccion resultado = gestor.traducirDireccion(t.getProcesoId(), t.getDireccionVirtual());
                mostrarResultadoTraduccion(resultado);
            }

            agregarLog("Archivo cargado: " + archivo.getName());
            actualizarVista();
        } catch (Exception e) {
            mostrarAlerta("Error al leer archivo: " + e.getMessage());
        }
    }

    private void inicializarGestor() {
        gestor = new GestorPaginacion(spinnerMemoriaFisica.getValue(), comboTamanoPagina.getValue());
        comboProcesoTraducir.getItems().clear();
        SpinnerValueFactory.IntegerSpinnerValueFactory marcoFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, gestor.getTotalMarcos() - 1, 0, 1);
        spinnerMarcoAsignar.setValueFactory(marcoFactory);
    }

    private void actualizarVista() {
        renderizarMarcos();
        actualizarTablaPaginas();
        actualizarListaMarcosDisponibles();
    }

    private void renderizarMarcos() {
        gridMarcos.getChildren().clear();
        int columnas = 4;
        int fila = 0;
        int columna = 0;

        for (Marco marco : gestor.getMarcos()) {
            VBox celda = new VBox(4);
            celda.setAlignment(Pos.CENTER);
            celda.getStyleClass().add(marco.isLibre() ? "marco-libre" : "marco-ocupado");
            celda.setPrefSize(140, 80);

            Label lblNum = new Label("Marco " + marco.getNumero());
            lblNum.getStyleClass().add("marco-titulo");

            if (marco.isLibre()) {
                celda.getChildren().addAll(lblNum, new Label("LIBRE"));
            } else {
                celda.getChildren().addAll(
                        lblNum,
                        new Label(marco.getIdProceso()),
                        new Label("Pág. " + marco.getNumeroPagina())
                );
            }

            gridMarcos.add(celda, columna, fila);
            columna++;
            if (columna >= columnas) {
                columna = 0;
                fila++;
            }
        }
    }

    private void actualizarTablaPaginas() {
        tablaPaginas.getItems().clear();
        String id = comboProcesoTraducir.getValue();
        if (id == null) {
            return;
        }

        TablaPaginas tabla = gestor.getTablasProcesos().get(id);
        if (tabla == null) {
            return;
        }

        for (EntradaPagina entrada : tabla.getEntradas()) {
            tablaPaginas.getItems().add(new FilaTablaPaginas(
                    entrada.getNumeroPagina(),
                    entrada.isValida() ? entrada.getNumeroMarco() : -1,
                    entrada.isValida() ? "V=1" : "V=0"
            ));
        }
    }

    private void actualizarListaMarcosDisponibles() {
        listaMarcos.getItems().clear();
        for (Integer marco : gestor.getMarcosDisponibles()) {
            listaMarcos.getItems().add("Marco " + marco);
        }
        if (listaMarcos.getItems().isEmpty()) {
            listaMarcos.getItems().add("Sin marcos disponibles");
        }
    }

    private void mostrarResultadoTraduccion(ResultadoTraduccion resultado) {
        if (resultado.isExito()) {
            lblResultadoTraduccion.setText(String.format(
                    "DV %d → DF %d  |  Página: %d  |  Offset: %d  |  Marco: %d",
                    resultado.getDireccionVirtual(), resultado.getDireccionFisica(),
                    resultado.getNumeroPagina(), resultado.getDesplazamiento(), resultado.getNumeroMarco()));
            lblResultadoTraduccion.getStyleClass().removeAll("resultado-error", "resultado-fallo");
            if (!lblResultadoTraduccion.getStyleClass().contains("resultado-exito")) {
                lblResultadoTraduccion.getStyleClass().add("resultado-exito");
            }
        } else if (resultado.isFalloPagina()) {
            lblResultadoTraduccion.setText("FALLO DE PÁGINA — Página " + resultado.getNumeroPagina() + " no cargada");
            lblResultadoTraduccion.getStyleClass().removeAll("resultado-exito", "resultado-error");
            if (!lblResultadoTraduccion.getStyleClass().contains("resultado-fallo")) {
                lblResultadoTraduccion.getStyleClass().add("resultado-fallo");
            }
        } else {
            lblResultadoTraduccion.setText(resultado.getMensaje());
            lblResultadoTraduccion.getStyleClass().removeAll("resultado-exito", "resultado-fallo");
            if (!lblResultadoTraduccion.getStyleClass().contains("resultado-error")) {
                lblResultadoTraduccion.getStyleClass().add("resultado-error");
            }
        }

        StringBuilder sb = new StringBuilder();
        for (PasoTraduccion paso : resultado.getPasos()) {
            sb.append(paso).append("\n\n");
        }
        areaPasos.setText(sb.toString());
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

    /**
     * Fila para la TableView de la tabla de páginas.
     */
    public static class FilaTablaPaginas {
        private final int pagina;
        private final int marco;
        private final String valida;

        public FilaTablaPaginas(int pagina, int marco, String valida) {
            this.pagina = pagina;
            this.marco = marco;
            this.valida = valida;
        }

        public int getPagina() {
            return pagina;
        }

        public int getMarco() {
            return marco;
        }

        public String getValida() {
            return valida;
        }
    }
}
