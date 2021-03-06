package mh.shiftcalendaram;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

import java.util.ArrayList;

import mh.calendarlibrary.Schemes;
import mh.calendarlibrary.Database.Database;
import mh.calendarlibrary.Templates.AccountTemplate;

public class CreateAccountFormActivity extends AppCompatActivity {

    TextInputLayout nameLayout, schemeLayout;
    EditText name, scheme, desc;


    MultiStateToggleButton allDaySwitch;
    ImageView palette;
    AppBarLayout head;

    int selectedColor;
    String strColor;

    boolean edit = false;
    Database database;

    //database section
    int positionOfScheme = - 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        database = new Database(CreateAccountFormActivity.this);

        name = (EditText)findViewById(R.id.edit_text_createAccount_name);
        nameLayout = (TextInputLayout)findViewById(R.id.input_layout_createAccount_name);
        scheme = (EditText)findViewById(R.id.edit_text_createAccount_scheme);
        schemeLayout = (TextInputLayout)findViewById(R.id.input_layout_createAccount_scheme);
        desc = (EditText)findViewById(R.id.edit_text_createAccount_desc);

        head = (AppBarLayout)findViewById(R.id.create_account_head);
        palette = (ImageView)findViewById(R.id.imageView_palette);

        allDaySwitch = (MultiStateToggleButton) this.findViewById(R.id.toggleButton_createAccount_scheme);
        allDaySwitch.setValue(0);
        allDaySwitch.setEnabled(false);

        int listPosition = getIntent().getIntExtra("position", -1);
        if(listPosition != -1) {
            edit = true;
            getDataToEdit(listPosition);
        }



        //TODO: upravit barvy v pickeru, a vychozí barvu
        selectedColor = ContextCompat.getColor(getBaseContext(), R.color.colorPrimary);
        strColor = String.format("#%06X", 0xFFFFFF & selectedColor);
        int[] mColors = getResources().getIntArray(R.array.rainbow);


        //TODO: (nespecha)upravit ColorPicker aby se neopakoval ve vsech activitach
        final ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                mColors,
                selectedColor,
                5,
                ColorPickerDialog.SIZE_SMALL);


        palette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usePalette(dialog);

            }
        });

        scheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateAccountFormActivity.this, SchemeListActivity.class);
                intent.putExtra("showSchemeGroup", false);
                startActivityForResult(intent, 0);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.ic_accept) {
            if(CheckAndSetErrors() == true) {
                database.insertAccount(name.getText().toString(), Schemes.getStringValueOfTypeSwitch(allDaySwitch.getValue()), positionOfScheme, strColor, desc.getText().toString());
                setResult(1000, null);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void usePalette(ColorPickerDialog dialog) {

        dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

            @Override
            public void onColorSelected(int color) {
                selectedColor = color;
                head.setBackgroundColor(selectedColor);
                strColor = String.format("#%06X", 0xFFFFFF & selectedColor);

               colorHeader(selectedColor);
            }
        });

        dialog.show(getFragmentManager(), "color_dialog_test");

    }

    public void colorHeader(int chooseColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Colors.convertColorToDark(chooseColor));
        }
    }
    public boolean CheckAndSetErrors() {

        boolean isOk = true;

        if(name.getText().length() == 0) {
            isOk = false;
            nameLayout.setError("Vyplňte název");
        } else {
            nameLayout.setErrorEnabled(false);
        }

        if(positionOfScheme == -1) {
            isOk = false;
            schemeLayout.setError("Zvolte pracoviště");
        } else {
            schemeLayout.setErrorEnabled(false);
        }

        return isOk;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(resultCode == 1) {
            positionOfScheme = data.getIntExtra("positionOfScheme", -1);

            ArrayList<Schemes> schemes = Schemes.createList();

            scheme.setText(Schemes.getStringArray().get(positionOfScheme));
            allDaySwitch.setElements(schemes.get(positionOfScheme).getTypesNames(schemes.get(positionOfScheme).getNumberOfSchemes()));
            allDaySwitch.setValue(0);
            allDaySwitch.setEnabled(true);
            allDaySwitch.setAlpha(1f);
        }
    }

    //TODO: upravit
    public void getDataToEdit(int listPosition) {
        ArrayList<AccountTemplate> accounts = database.getAccounts();
        name.setText(accounts.get(listPosition).getName());
        String type = accounts.get(listPosition).getShiftSchemeGroup();
        allDaySwitch.setEnabled(true);
        allDaySwitch.setAlpha(1f);
        switch (type) {
            case "A": allDaySwitch.setValue(0); break;
            case "B": allDaySwitch.setValue(1); break;
            case "C": allDaySwitch.setValue(2); break;
            case "D": allDaySwitch.setValue(3); break;
        }
        ArrayList<String> schemes = Schemes.getStringArray();
        positionOfScheme = accounts.get(listPosition).getShiftSchemeID();
        scheme.setText(schemes.get(positionOfScheme));
        //TODO: dodelat desc (note)
        //TODO: nahradit pozice v listu ID z databáze
        selectedColor = accounts.get(listPosition).getColor();
        head.setBackgroundColor(selectedColor);
        colorHeader(selectedColor);
        strColor = accounts.get(listPosition).getColorHex();





    }

    //TODO:aplikovat setshadows všude
    public void setShadows() {
        View shadow = (View)findViewById(R.id.shadow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shadow.setVisibility(View.GONE);
        }
    }
}
