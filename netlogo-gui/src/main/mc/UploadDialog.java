// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class UploadDialog extends JDialog {

  //GUI form members
  private JLabel personNameLabel;
  private JLabel errorLabel;
  private JButton uploadModelButton;
  private JButton cancelButton;
  private JButton logoutButton;
  private JTextField modelNameField;
  private JComboBox<Group> groupComboBox;
  private DisableableComboBox visibilityComboBox;
  private DisableableComboBox changeabilityComboBox;
  private JRadioButton useCurrentViewRadioButton;
  private JRadioButton autoGenerateViewRadioButton;
  private JRadioButton noPreviewRadioButton;
  private JRadioButton imageFromFileRadioButton;
  private JLabel autoGenerateDisabledExplanation;
  private JRadioButton newModelRadioButton;
  private JRadioButton childOfExistingModelRadioButton;
  private JRadioButton newVersionOfExistingRadioButton;
  private DisableableComboBox existingModelNameComboBox;
  private JTextField existingModelNameSearchField;
  private JTextField descriptionTextField;
  private JLabel modelNameLabel;
  private JLabel existingModelNameLabel;
  private JLabel descriptionLabel;
  private JLabel modelGroupLabel;
  private JLabel visibilityLabel;
  private JLabel changeabilityLabel;
  private JLabel previewImageLabel;
  private ButtonGroup uploadTypeButtonGroup;
  private ButtonGroup previewImageButtonGroup;
  private FileSelector fileSelector;
  private JPanel topLevelContainer;

  //Data members
  private ModelingCommons communicator;
  private int groupPermissionIndex;
  private int userPermissionIndex;
  private int everyonePermissionIndex;
  private Request currentModelSearchRequest;
  private String nextModelSearchString;
  private boolean enableAutoGeneratePreviewImage;
  private Frame frame;

  public UploadDialog(Frame frame, ModelingCommons communicator, String errorLabelText, boolean enableAutoGeneratePreviewImage) {
    super(frame, "Upload Model to Modeling Commons", true);
    this.frame = frame;
    this.communicator = communicator;
    this.enableAutoGeneratePreviewImage = enableAutoGeneratePreviewImage;
    initializeGUIComponents();
    getRootPane().setDefaultButton(uploadModelButton);
    errorLabel.setText(errorLabelText);
    personNameLabel.setText("Hello " + communicator.getPerson().getFirstName() + " " + communicator.getPerson().getLastName());
    uploadModelButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        onOK();
      }

    });
    cancelButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        onCancel();
      }

    });
    logoutButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        dispose();
        Request request = new LogoutRequest(UploadDialog.this.communicator.getHttpClient(), UploadDialog.this.frame) {
          @Override
          protected void onLogout(String status, boolean logoutSuccessful) {
            if(logoutSuccessful) {
              UploadDialog.this.communicator.setPerson(null);
            }
            UploadDialog.this.communicator.promptForLogin();
          }
        };
        request.execute();
      }

    });
    List<Group> groups = new ArrayList<Group>(communicator.getGroups());
    groups.add(0, null);
    groupComboBox.setModel(new DefaultComboBoxModel<Group>(groups.toArray(new Group[0])));
    everyonePermissionIndex = visibilityComboBox.addItem(Permission.getPermissions().get("a"), true);
    changeabilityComboBox.addItem(Permission.getPermissions().get("a"), true);
    groupPermissionIndex = visibilityComboBox.addItem(Permission.getPermissions().get("g"), false);
    changeabilityComboBox.addItem(Permission.getPermissions().get("g"), false);
    userPermissionIndex = visibilityComboBox.addItem(Permission.getPermissions().get("u"), true);
    changeabilityComboBox.addItem(Permission.getPermissions().get("u"), true);
    groupComboBox.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        boolean groupSelected = !(groupComboBox.getSelectedItem() == null);
        visibilityComboBox.setIndexEnabled(groupPermissionIndex, groupSelected);
        changeabilityComboBox.setIndexEnabled(groupPermissionIndex, groupSelected);
        Permission visibility = (Permission) (visibilityComboBox.getSelectedObject());
        if(!groupSelected && visibility.getId().equals("g")) {
          visibilityComboBox.setSelectedIndex(userPermissionIndex);
        }
        Permission changeability = (Permission) (changeabilityComboBox.getSelectedObject());
        if(!groupSelected && changeability.getId().equals("g")) {
          changeabilityComboBox.setSelectedIndex(userPermissionIndex);
        }
      }

    });
    useCurrentViewRadioButton.setSelected(true);
    fileSelector.setEnabled(false);
    imageFromFileRadioButton.addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        fileSelector.setEnabled(imageFromFileRadioButton.isSelected());
      }

    });
    if(!enableAutoGeneratePreviewImage) {
      autoGenerateViewRadioButton.setEnabled(false);
    } else {
      autoGenerateDisabledExplanation.setVisible(false);
    }
    existingModelNameSearchField.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void insertUpdate(DocumentEvent documentEvent) {
        updateExistingModelNameComboBox(existingModelNameSearchField.getText());
      }

      @Override
      public void removeUpdate(DocumentEvent documentEvent) {
        updateExistingModelNameComboBox(existingModelNameSearchField.getText());
      }

      @Override
      public void changedUpdate(DocumentEvent documentEvent) {}

    });
    newModelRadioButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        if(newModelRadioButton.isSelected()) {
          setNewModelMode();
        }
      }

    });
    newVersionOfExistingRadioButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        if(newVersionOfExistingRadioButton.isSelected()) {
          setNewVersionMode();
        }
      }

    });
    childOfExistingModelRadioButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        if(childOfExistingModelRadioButton.isSelected()) {
          setChildMode();
        }
      }

    });
    newModelRadioButton.setSelected(true);
    setNewModelMode();
    updateExistingModelNameComboBox("");
    //call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });
    //call onCancel() on ESCAPE
    topLevelContainer.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    this.pack();
    this.setLocationRelativeTo(frame);
    this.setResizable(false);
  }

  //BEGIN model search functions

  private void setNextModelSearch(String searchString) {
    nextModelSearchString = searchString;
    if(currentModelSearchRequest == null) {
      executeModelSearch();
    } else {
      currentModelSearchRequest.abort();
      //Once the abort request is done, the aborted request will execute the next search request
    }
  }

  private void executeModelSearch() {
    boolean ensureChangeabilityPermission = getSelectedUploadType() == NewModelType.NEW_VERSION;
    currentModelSearchRequest = new SearchForModelsRequest(communicator.getHttpClient(), nextModelSearchString, 10, ensureChangeabilityPermission) {
      @Override
      protected void onSearchResults(String status, List<Model> models) {
        if(status.equals("ABORTED_OR_CONNECTION_ERROR")) {
          //Start the next search after the current search finishes aborting
          if(nextModelSearchString != null) {
            executeModelSearch();
          } else {
            //If we get an aborted or connection error and we don't have another search queued, then
            //we know that there was a connection error since abortions only happen when a search is queued
            //while an existing search is still executing
            currentModelSearchRequest = null;
            clearExistingModelNameComboBox();
            existingModelNameComboBox.addItem("Error connecting to Modeling Commons", false);

          }
        } else if(status.equals("INVALID_RESPONSE_FROM_SERVER")) {
          clearExistingModelNameComboBox();
          existingModelNameComboBox.addItem("Invalid response from Modeling Commons", false);
        } else if(status.equals("SUCCESS")) {
          existingModelNameComboBox.removeAllItems();
          if(models.size() > 0) {
            for(Model model : models) {
              existingModelNameComboBox.addItem(model, true);
            }
            boolean currentFocus = existingModelNameSearchField.hasFocus();
            existingModelNameComboBox.showPopup();
            if(currentFocus) {
              existingModelNameSearchField.requestFocus();
            }
          } else {
            existingModelNameComboBox.addItem("No existing models found", false);
          }
          currentModelSearchRequest = null;
          nextModelSearchString = null;
        }
      }

    };
    nextModelSearchString = null;
    currentModelSearchRequest.execute();
  }

  private void clearExistingModelNameComboBox() {
    existingModelNameComboBox.removeAllItems();
    existingModelNameComboBox.hidePopup();
  }

  private void updateExistingModelNameComboBox(String searchString) {
    clearExistingModelNameComboBox();
    if(searchString.length() > 0) {
      existingModelNameComboBox.addItem("Searching", false);
      setNextModelSearch(searchString);
    } else {
      existingModelNameComboBox.addItem("Enter name of existing model", false);
      if(currentModelSearchRequest != null) {
        currentModelSearchRequest.abort();
      }
    }
  }

  //END model search functions

  private boolean isValidInput() {
    NewModelType uploadType = getSelectedUploadType();
    //Check name
    if(uploadType == NewModelType.NEW || uploadType == NewModelType.CHILD) {
      if(modelNameField.getText().trim().length() == 0) {
        errorLabel.setText("Missing model name");
        return false;
      }
    }
    //Check existing model is valid
    if(uploadType == NewModelType.NEW_VERSION || uploadType == NewModelType.CHILD) {
      if(getSelectedExistingModel() == null) {
        if(uploadType == NewModelType.NEW_VERSION) {
          errorLabel.setText("Must select an existing model to make a new version of");
        } else {
          errorLabel.setText("Must select an existing parent model");
        }
        return false;
      }
    }
    //Check description
    if(uploadType == NewModelType.NEW_VERSION || uploadType == NewModelType.CHILD) {
      if(descriptionTextField.getText().trim().length() == 0) {
        errorLabel.setText("Description cannot be blank");
        return false;
      }
    }
    return true;
  }

  private void onOK() {
    if(!isValidInput()) {
      return;
    }
    dispose();
    String modelName = modelNameField.getText().trim();
    String description = descriptionTextField.getText().trim();
    Group group = (Group) groupComboBox.getSelectedItem();
    Permission visibility = (Permission) visibilityComboBox.getSelectedObject();
    Permission changeability = (Permission) changeabilityComboBox.getSelectedObject();
    NewModelType uploadType = getSelectedUploadType();
    Model selectedExistingModel = getSelectedExistingModel();
    int existingModelId = -1;
    if(selectedExistingModel != null) {
      existingModelId = selectedExistingModel.getId();
    }
    Image previewImage = null;
    if(useCurrentViewRadioButton.isSelected()) {
      previewImage = communicator.getCurrentModelViewImage();
    } else if(imageFromFileRadioButton.isSelected()) {
      if(fileSelector.getFilePath() != null) {
        previewImage = new FileImage(fileSelector.getFilePath());
      }
    } else if(autoGenerateViewRadioButton.isSelected()) {
      previewImage = ImageGenerator.getAutoGeneratedModelImage(
        communicator.workspaceFactory);
    }
    if(uploadType == NewModelType.NEW) {
      Request request = new UploadModelRequest(
          communicator.getHttpClient(),
          frame,
          modelName,
          communicator.getModelBody(),
          group,
          visibility,
          changeability,
          previewImage
      ) {

        @Override
        protected void onUploaded(String status, String uploadedModelURL, String uploadedModelName) {
          if(status.equals("NOT_LOGGED_IN")) {
            communicator.promptForLogin();
          } else if(status.equals("MISSING_PARAMETERS")) {
            communicator.promptForUpload("Missing model name");
          } else if(status.equals("MODEL_NOT_SAVED")) {
            communicator.promptForUpload("Server error");
          } else if(status.equals("INVALID_RESPONSE_FROM_SERVER")) {
            communicator.promptForUpload("Invalid response from Modeling Commons", false);
          } else if(status.equals("CONNECTION_ERROR")) {
            communicator.promptForUpload("Could not connect to Modeling Commons", false);
          } else if(status.equals("SUCCESS")) {
            communicator.promptForSuccess(uploadedModelURL, uploadedModelName);
          } else if(status.equals("INVALID_PREVIEW_IMAGE")) {
            communicator.promptForUpload("Invalid preview image");
          } else if(status.equals("SUCCESS_PREVIEW_NOT_SAVED")) {
            communicator.promptForSuccess("The model was uploaded, but the preview image was not saved", uploadedModelURL, uploadedModelName);
          } else {
            communicator.promptForUpload("Unknown server error", false);
          }
        }

      };
      request.execute();
    } else if(uploadType == NewModelType.NEW_VERSION || uploadType == NewModelType.CHILD) {
      Request request = new UpdateModelRequest(
          communicator.getHttpClient(),
          frame,
          existingModelId,
          modelName,
          communicator.getModelBody(),
          description,
          uploadType
      ) {

        @Override
        protected void onUploaded(String status, String uploadedModelURL, String uploadedModelName) {
          if(status.equals("NOT_LOGGED_IN")) {
            communicator.promptForLogin();
          } else if(status.equals("MISSING_PARAMETERS")) {
            communicator.promptForUpload("Missing parameters");
          } else if(status.equals("MODEL_NOT_SAVED")) {
            communicator.promptForUpload("Server error");
          } else if(status.equals("INVALID_RESPONSE_FROM_SERVER")) {
            communicator.promptForUpload("Invalid response from Modeling Commons", false);
          } else if(status.equals("CONNECTION_ERROR")) {
            communicator.promptForUpload("Could not connect to Modeling Commons", false);
          } else if(status.equals("SUCCESS")) {
            communicator.promptForSuccess(uploadedModelURL, uploadedModelName);
          } else {
            communicator.promptForUpload("Unknown server error", false);
          }
        }

      };
      request.execute();
    }
  }

  private void onCancel() {
    dispose();
  }

  private NewModelType getSelectedUploadType() {
    if(newModelRadioButton.isSelected()) {
      return NewModelType.NEW;
    } else if(newVersionOfExistingRadioButton.isSelected()) {
      return NewModelType.NEW_VERSION;
    } else if(childOfExistingModelRadioButton.isSelected()) {
      return NewModelType.CHILD;
    } else {
      return null;
    }
  }

  private Model getSelectedExistingModel() {
    Object selectedModel = existingModelNameComboBox.getSelectedObject();
    if(selectedModel != null && selectedModel instanceof Model) {
      return (Model) selectedModel;
    } else {
      return null;
    }
  }

  //BEGIN model type mode functions

  private void modelNameSetEnabled(boolean enabled) {
    modelNameField.setEnabled(enabled);
    modelNameLabel.setEnabled(enabled);
  }

  private void existingModelSetEnabled(boolean enabled) {
    existingModelNameSearchField.setEnabled(enabled);
    existingModelNameComboBox.setEnabled(enabled);
    existingModelNameLabel.setEnabled(enabled);
    existingModelNameSearchField.setText("");
  }

  private void descriptionSetEnabled(boolean enabled) {
    descriptionTextField.setEnabled(enabled);
    descriptionLabel.setEnabled(enabled);
  }

  private void permissionsSetEnabled(boolean enabled) {
    groupComboBox.setEnabled(enabled);
    visibilityComboBox.setEnabled(enabled);
    changeabilityComboBox.setEnabled(enabled);
    modelGroupLabel.setEnabled(enabled);
    visibilityLabel.setEnabled(enabled);
    changeabilityLabel.setEnabled(enabled);
  }

  private void previewImageSetEnabled(boolean enabled) {
    useCurrentViewRadioButton.setEnabled(enabled);
    autoGenerateViewRadioButton.setEnabled(enabled && enableAutoGeneratePreviewImage);
    imageFromFileRadioButton.setEnabled(enabled);
    fileSelector.setEnabled(enabled && imageFromFileRadioButton.isSelected());
    noPreviewRadioButton.setEnabled(enabled);
    previewImageLabel.setEnabled(enabled);
    autoGenerateDisabledExplanation.setEnabled(enabled);
  }

  private void setNewModelMode() {
    modelNameSetEnabled(true);
    existingModelSetEnabled(false);
    descriptionSetEnabled(false);
    permissionsSetEnabled(true);
    previewImageSetEnabled(true);
  }

  private void setNewVersionMode() {
    modelNameSetEnabled(false);
    existingModelSetEnabled(true);
    descriptionSetEnabled(true);
    permissionsSetEnabled(false);
    previewImageSetEnabled(false);
  }

  private void setChildMode() {
    modelNameSetEnabled(true);
    existingModelSetEnabled(true);
    descriptionSetEnabled(true);
    permissionsSetEnabled(false);
    previewImageSetEnabled(false);
  }

  //END model type mode functions

  private void setMaxHeightToPreferredHeight(JComponent component) {
    component.setMaximumSize(new Dimension((int) component.getMaximumSize().getWidth(), (int) component.getPreferredSize().getHeight()));
  }

  private void initializeGUIComponents() {
    topLevelContainer = new JPanel();
    topLevelContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    topLevelContainer.setLayout(new BoxLayout(topLevelContainer, BoxLayout.Y_AXIS));
    setContentPane(topLevelContainer);

    JPanel helloPanel = new JPanel();
    JPanel formPanel = new JPanel();
    JPanel errorPanel = new JPanel();
    JPanel buttonsPanel = new JPanel();
    topLevelContainer.add(helloPanel);
    topLevelContainer.add(formPanel);
    topLevelContainer.add(Box.createVerticalGlue());
    topLevelContainer.add(errorPanel);
    topLevelContainer.add(buttonsPanel);

    helloPanel.setLayout(new BoxLayout(helloPanel, BoxLayout.X_AXIS));
    personNameLabel = new JLabel("Hello Firstname Lastname");
    helloPanel.add(personNameLabel);
    helloPanel.add(Box.createHorizontalGlue());
    setMaxHeightToPreferredHeight(helloPanel);

    formPanel.setLayout(new BorderLayout());
    JPanel formLabels = new JPanel(new GridLayout(17, 1));
    JPanel formFields = new JPanel(new GridLayout(17, 1));
    formLabels.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
    formPanel.add(formLabels, BorderLayout.LINE_START);
    formPanel.add(formFields, BorderLayout.CENTER);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1;

    formLabels.add(new JLabel("Upload As"));
    newModelRadioButton = new JRadioButton("New model");
    formFields.add(newModelRadioButton);

    formLabels.add(new JLabel(""));
    childOfExistingModelRadioButton = new JRadioButton("Child of existing model");
    formFields.add(childOfExistingModelRadioButton);

    formLabels.add(new JLabel(""));
    newVersionOfExistingRadioButton = new JRadioButton("New version of existing model");
    formFields.add(newVersionOfExistingRadioButton);

    formLabels.add(new JLabel(""));
    formFields.add(new JLabel(""));

    modelNameLabel = new JLabel("New Model Name");
    formLabels.add(modelNameLabel);
    modelNameField = new JTextField();
    JPanel modelNameFieldPanel = new JPanel(new GridBagLayout());
    modelNameFieldPanel.add(modelNameField, constraints);
    formFields.add(modelNameFieldPanel);

    existingModelNameLabel = new JLabel("Existing Model Name");
    formLabels.add(existingModelNameLabel);
    existingModelNameSearchField = new JTextField();
    JPanel existingModelNameSearchFieldPanel = new JPanel(new GridBagLayout());
    existingModelNameSearchFieldPanel.add(existingModelNameSearchField, constraints);
    formFields.add(existingModelNameSearchFieldPanel);

    formLabels.add(new JLabel(""));
    existingModelNameComboBox = new DisableableComboBox();
    JPanel existingModelNameComboBoxPanel = new JPanel(new GridBagLayout());
    existingModelNameComboBoxPanel.add(existingModelNameComboBox, constraints);
    formFields.add(existingModelNameComboBoxPanel);

    descriptionLabel = new JLabel("Short Comment");
    formLabels.add(descriptionLabel);
    descriptionTextField = new JTextField();
    JPanel descriptionTextFieldPanel = new JPanel(new GridBagLayout());
    descriptionTextFieldPanel.add(descriptionTextField, constraints);
    formFields.add(descriptionTextFieldPanel);

    formLabels.add(new JLabel(""));
    formFields.add(new JLabel(""));

    modelGroupLabel = new JLabel("Model Group");
    formLabels.add(modelGroupLabel);
    groupComboBox = new JComboBox<Group>();
    JPanel groupComboBoxPanel = new JPanel(new GridBagLayout());
    groupComboBoxPanel.add(groupComboBox, constraints);
    formFields.add(groupComboBoxPanel);

    visibilityLabel = new JLabel("Visible By");
    formLabels.add(visibilityLabel);
    visibilityComboBox = new DisableableComboBox();
    JPanel visibilityComboBoxPanel = new JPanel(new GridBagLayout());
    visibilityComboBoxPanel.add(visibilityComboBox, constraints);
    formFields.add(visibilityComboBoxPanel);

    changeabilityLabel = new JLabel("Changeable By");
    formLabels.add(changeabilityLabel);
    changeabilityComboBox = new DisableableComboBox();
    JPanel changeabilityComboBoxPanel = new JPanel(new GridBagLayout());
    changeabilityComboBoxPanel.add(changeabilityComboBox, constraints);
    formFields.add(changeabilityComboBoxPanel);

    formLabels.add(new JLabel(""));
    formFields.add(new JLabel(""));

    previewImageLabel = new JLabel("Preview Image");
    formLabels.add(previewImageLabel);
    useCurrentViewRadioButton = new JRadioButton("Use current image");
    formFields.add(useCurrentViewRadioButton);

    formLabels.add(new JLabel(""));
    JPanel autoGeneratePanel = new JPanel();
    autoGeneratePanel.setLayout(new BoxLayout(autoGeneratePanel, BoxLayout.X_AXIS));
    formFields.add(autoGeneratePanel);
    autoGenerateViewRadioButton = new JRadioButton("Auto-generate image");
    autoGenerateDisabledExplanation = new JLabel("Setup and go procedures must be defined to auto-generate");
    autoGeneratePanel.add(autoGenerateViewRadioButton);
    autoGeneratePanel.add(Box.createRigidArea(new Dimension(10, 0)));
    autoGeneratePanel.add(autoGenerateDisabledExplanation);

    formLabels.add(new JLabel(""));
    JPanel selectFilePanel = new JPanel();
    selectFilePanel.setLayout(new BoxLayout(selectFilePanel, BoxLayout.X_AXIS));
    formFields.add(selectFilePanel);
    imageFromFileRadioButton = new JRadioButton("Image from file");
    selectFilePanel.add(imageFromFileRadioButton);
    fileSelector = new FileSelector(selectFilePanel);
    selectFilePanel.add(fileSelector);

    formLabels.add(new JLabel(""));
    noPreviewRadioButton = new JRadioButton("No preview image");
    formFields.add(noPreviewRadioButton);

    setMaxHeightToPreferredHeight(formPanel);

    errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.X_AXIS));
    errorLabel = new JLabel("Error! ");
    errorLabel.setForeground(Color.RED);
    errorPanel.add(errorLabel);
    errorPanel.add(Box.createHorizontalGlue());
    setMaxHeightToPreferredHeight(errorPanel);

    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
    logoutButton = new JButton("Logout");
    buttonsPanel.add(logoutButton);
    buttonsPanel.add(Box.createHorizontalGlue());
    cancelButton = new JButton("Cancel");
    buttonsPanel.add(cancelButton);
    buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    uploadModelButton = new JButton("Upload Model");
    buttonsPanel.add(uploadModelButton);
    setMaxHeightToPreferredHeight(buttonsPanel);

    uploadTypeButtonGroup = new ButtonGroup();
    uploadTypeButtonGroup.add(newModelRadioButton);
    uploadTypeButtonGroup.add(childOfExistingModelRadioButton);
    uploadTypeButtonGroup.add(newVersionOfExistingRadioButton);

    previewImageButtonGroup = new ButtonGroup();
    previewImageButtonGroup.add(useCurrentViewRadioButton);
    previewImageButtonGroup.add(autoGenerateViewRadioButton);
    previewImageButtonGroup.add(imageFromFileRadioButton);
    previewImageButtonGroup.add(noPreviewRadioButton);
  }

}

