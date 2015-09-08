package com.metacodestudio.hotsuploader.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * @author Mateus Viccari
 * @author Eivind Vegsundvåg
 */
public class FXUtils {

    public enum AutoCompleteMode {
        STARTS_WITH,CONTAINING,;
    }

    public static<T> void autoCompleteComboBox(ComboBox<T> comboBox, AutoCompleteMode mode) {
        ObservableList<T> data = comboBox.getItems();

        comboBox.setEditable(true);
        comboBox.getEditor().focusedProperty().addListener(observable -> {
            if (0 > comboBox.getSelectionModel().getSelectedIndex()) {
                comboBox.getEditor().setText(null);
            }
        });
        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, t -> comboBox.hide());
        comboBox.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {

            private boolean moveCaretToPos = false;
            private int caretPos;

            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case DOWN:
                        if (!comboBox.isShowing()) {
                            comboBox.show();
                        }
                    case UP:
                        caretPos = -1;
                        moveCaret(comboBox.getEditor().getText().length());
                        return;
                    case BACK_SPACE:
                    case DELETE:
                        moveCaretToPos = true;
                        caretPos = comboBox.getEditor().getCaretPosition();
                        break;
                }

                if (KeyCode.RIGHT == event.getCode() || KeyCode.LEFT == event.getCode()
                        || event.isControlDown() || KeyCode.HOME == event.getCode()
                        || KeyCode.END == event.getCode() || KeyCode.TAB == event.getCode()) {
                    return;
                }

                ObservableList<T> list = FXCollections.observableArrayList();
                for (T aData : data) {
                    if (shouldDataBeAddedToInput(aData)) {
                        list.add(aData);
                    }
                }
                String t = comboBox.getEditor().getText();

                comboBox.setItems(list);
                comboBox.getEditor().setText(t);
                if (!moveCaretToPos) {
                    caretPos = -1;
                }
                moveCaret(t.length());
                if (!list.isEmpty()) {
                    comboBox.show();
                }
            }

            private boolean shouldDataBeAddedToInput(T aData) {
                return mode.equals(AutoCompleteMode.STARTS_WITH) && inputStartsWith(aData)
                        || mode.equals(AutoCompleteMode.CONTAINING) && inputContains(aData);
            }

            private boolean inputStartsWith(T aData) {
                String dataValue = aData.toString().toLowerCase();
                String inputValue = comboBox.getEditor().getText().toLowerCase();
                return dataValue.startsWith(inputValue);
            }

            private boolean inputContains(T aData) {
                String dataValue = aData.toString().toLowerCase();
                String inputValue = comboBox.getEditor().getText().toLowerCase();
                return dataValue.contains(inputValue);
            }

            private void moveCaret(int textLength) {
                if (-1 == caretPos) {
                    comboBox.getEditor().positionCaret(textLength);
                } else {
                    comboBox.getEditor().positionCaret(caretPos);
                }
                moveCaretToPos = false;
            }
        });
    }
}
