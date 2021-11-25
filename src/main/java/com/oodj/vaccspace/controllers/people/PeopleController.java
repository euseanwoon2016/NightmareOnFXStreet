package com.oodj.vaccspace.controllers.people;

import com.oodj.vaccspace.models.Person;
import com.oodj.vaccspace.models.VaccinationStatus;
import com.oodj.vaccspace.utils.StringHelper;
import com.oodj.vaccspace.utils.TableHelper;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import textorm.TextORM;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class PeopleController implements Initializable {

    PeopleViewModel vm = new PeopleViewModel();

    ObservableList<Person> masterData;

    FilteredList<Person> filteredData;

    SortedList<Person> sortableData;

    @FXML
    private TableView<Person> tblPeople;

    @FXML
    private MFXTextField txtSearch;

    @FXML
    void onSearchChanged(KeyEvent event) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        vm.searchProperty().bindBidirectional(txtSearch.textProperty());

        vm.searchProperty().addListener((observableValue, s, t1) -> {
            filteredData.setPredicate(getFilterPersonPredicate());
        });

        TableColumn<Person, String> personName = new TableColumn<>("Full Name");
        personName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Person, String> identificationNumber = new TableColumn<>("Identification Number");
        identificationNumber.setCellValueFactory(new PropertyValueFactory<>("identification"));

        TableColumn<Person, String> phoneNumber = new TableColumn<>("Phone Number");
        phoneNumber.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Person, String> email = new TableColumn<>("Email");
        email.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Person, String> isNonCitizen = new TableColumn<>("Citizenship");
        isNonCitizen.setCellValueFactory(rowData -> {
            String citizenshipValue = rowData.getValue().isNonCitizen() ? "Non-citizen" : "Citizen";
            return new SimpleStringProperty(citizenshipValue);
        });

        TableColumn<Person, VaccinationStatus> status = new TableColumn<>("Status");
        status.setCellValueFactory(new PropertyValueFactory<>("vaccinationStatus"));
        status.setCellFactory(statusColumn -> new VaccinationStatusIndicatorCell());

        tblPeople.getColumns().addAll(personName, identificationNumber, phoneNumber, email, isNonCitizen, status);

        tblPeople.setRowFactory(tableView -> {
            TableRow<Person> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2 && (!row.isEmpty())) {
                    Person person = row.getItem();
                    System.out.println(person.getName());
                }
            });
            return row;
        });

        TableHelper.autoSizeColumns(tblPeople);

        List<Person> people = TextORM.getAll(Person.class, hashMap -> true);

        masterData = FXCollections.observableArrayList(people);
        filteredData = new FilteredList<>(masterData);
        sortableData = new SortedList<>(filteredData);

        tblPeople.setItems(sortableData);

        sortableData.comparatorProperty().bind(tblPeople.comparatorProperty());
    }

    private Predicate<Person> getFilterPersonPredicate() {
        return person -> StringHelper.containsIgnoreCase(person.getName(), vm.getSearch()) ||
                         StringHelper.containsIgnoreCase(person.getName(), vm.getSearch()) ||
                         StringHelper.containsIgnoreCase(person.getIdentification(), vm.getSearch()) ||
                         StringHelper.containsIgnoreCase(person.getVaccinationStatus().getValue(), vm.getSearch()) ||
                         StringHelper.containsIgnoreCase(person.getEmail(), vm.getSearch()) ||
                         StringHelper.containsIgnoreCase(person.getPhone(), vm.getSearch()) ||
                         StringHelper.containsIgnoreCase(
                                 person.isNonCitizen() ? "Non-citizen" : "Citizen",
                                 vm.getSearch()
                         );
    }
}
