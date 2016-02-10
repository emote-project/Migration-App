package hw.emote.dialogsystem;

import java.util.HashMap;

import uk.ac.hw.lirec.emys3d.EmysModel.Emotion;

public interface AndroidDialogProvider {
	public void speakText(String text);
	public void confirmDialog(String infoText);
	public void dismissConfirmDialog();
	public void askMultiChoice(Integer numChoices, String[] options);
	public void dismissMultiDialog();
	public void setExpression (Emotion em);
	public Emotion getEmysEmotion();
	
	public void migrateDataIn(HashMap<String,String> migData);
	public void navigateFromTo(String from, String to, String callback);
	public void getFreeTextDialog();
	public void putMigrationData(HashMap<String,String> data);
	public void playMigrateOutSound();
	public void playMigrateInSound();
	
}