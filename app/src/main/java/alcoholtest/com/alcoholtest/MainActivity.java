package alcoholtest.com.alcoholtest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

import alcoholtest.com.alcoholtest.adapter.DrinkAdapter;
import alcoholtest.com.alcoholtest.adapter.MixtureAdapter;
import alcoholtest.com.alcoholtest.adapter.UserAdapter;
import alcoholtest.com.alcoholtest.model.Drink;
import alcoholtest.com.alcoholtest.model.Mixture;
import alcoholtest.com.alcoholtest.model.User;

public class MainActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce;
    private Context c;
    private User currentUser;
    private Button btnAddDrink;
    private TextView tvName;
    private TextView tvAge;
    private TextView tvWeight;
    private TextView tvHeight;
    private TextView tvSex;
    private DrinkAdapter dA;
    private ListView drinks;
    private DecimalFormat format = new DecimalFormat();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        c = getApplicationContext();

        tvName = (TextView) findViewById(R.id.name);
        tvAge = (TextView) findViewById(R.id.age);
        tvWeight = (TextView) findViewById(R.id.weight);
        tvHeight = (TextView) findViewById(R.id.height);
        tvSex = (TextView) findViewById(R.id.sex);
        btnAddDrink = (Button) findViewById(R.id.add_drink_button);
        drinks = (ListView) findViewById(R.id.drinks);

        btnAddDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDrink();
            }
        });

        dA = new DrinkAdapter(this, new ArrayList<Drink>());
        drinks.setAdapter(dA);
        drinks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //Handle event: remove drink
                return false;
            }
        });

        format.setDecimalSeparatorAlwaysShown(false);
        switchUser(true);
        updateGui();
    }

    /**
     * Updates GUI to match the current selected user
     */
    public void updateGui() {
        if (currentUser != null) {
            tvName.setText(currentUser.getName());
            tvAge.setText(format.format(currentUser.getAge()) + " " + getString(R.string.years));
            tvWeight.setText(format.format(currentUser.getWeight()) + " kg");
            tvHeight.setText(format.format(currentUser.getHeight()) + " cm");

            if (currentUser.isMale()) {
                tvSex.setText(R.string.male);
            } else {
                tvSex.setText(R.string.female);
            }
            addDrinks();
        }
    }

    /**
     * Creates a user and opens select dialog afterwards
     */
    public void createUser() {
        startActivity(new Intent(this, CreateUser.class));
        finish();
    }

    /**
     * Removes given user. If that's the only user, the activity to create a now one will be launched.
     * If there were two users, the one left will be selected
     *
     * @param userToRemove user to remove
     * @throws JSONException if something bad happened (should not be the case)
     * @returns false if not successful, true if successful
     */
    public boolean removeUser(User userToRemove) throws JSONException {
        SharedPreferences sharedPref = getSharedPreferences("data", 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        JSONArray users = new JSONArray(sharedPref.getString("users", "[]"));

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = new JSONObject(users.get(i).toString());
            if (user.getString("name").compareTo(userToRemove.getName()) == 0) {
                users.remove(i);
                editor.putString("users", users.toString());
                editor.commit();

                if (users.length() == 0) {
                    createUser();
                }
                if (users.length() == 1) {
                    currentUser = new User(new JSONObject(users.get(0).toString()));
                    updateGui();
                } else {
                    switchUser(false);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Opens activity to edit the current user
     */
    public void editUser() {
        Intent i = new Intent(this, EditUser.class);
        i.putExtra("created", currentUser.getCreated());
        startActivity(i);
    }

    /**
     * Switchs between users. If there's no user, a new one will be created. If only one user exists, a toast will be shown
     *
     * @param fromStart If this method is called in the onCreate() methode, the "only one user existing" message will be suppressed.
     */
    public void switchUser(boolean fromStart) {
        try {
            SharedPreferences sharedPref = getSharedPreferences("data", 0);

            //Ist die Users-datenbank leer, wird ein neuer User erstellt
            if (sharedPref.getString("users", "[]").compareTo("[]") == 0) {
                createUser();
            } else {
                final JSONArray users = new JSONArray(sharedPref.getString("users", "[]"));

                //Wenn nur ein User vorhanden, wird dieser ausgewählt
                if (users.length() == 1) {
                    currentUser = new User(new JSONObject(users.get(0).toString()));
                    if (!fromStart) {
                        Toast.makeText(this, R.string.only_one_user_there, Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                final ArrayList<User> usersList = new ArrayList<>();
                for (int i = 0; i < users.length(); i++) {
                    usersList.add(new User(new JSONObject(users.get(i).toString())));
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.pick_user);
                builder.setNeutralButton(R.string.add_user, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createUser();
                    }
                });

                builder.setAdapter(new UserAdapter(getApplicationContext(), usersList),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int item) {
                                currentUser = usersList.get(item);
                                Toast.makeText(c, R.string.you_selected + " " + currentUser.getName(), Toast.LENGTH_LONG).show();

                                updateGui();
                                dialog.dismiss();
                            }
                        });
                Dialog alert = builder.create();
                alert.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //TODO: Find better name!!!

    public void addDrink() {

        try {
            SharedPreferences sharedPref = getSharedPreferences("data", 0);
            SharedPreferences.Editor editor = sharedPref.edit();
            JSONArray mixtures = new JSONArray(sharedPref.getString("mixtures", "[]"));

            if (mixtures.length() < 8) {
                //Updating mixtures
                //Add new mixtures here!
                mixtures = new JSONArray("[]");
                mixtures.put(new Mixture("Bier", 500, 0.05, "beer").toString());
                mixtures.put(new Mixture("Bier", 1000, 0.05, "morebeer").toString());
                mixtures.put(new Mixture("Pils", 330, 0.048, "pils").toString());
                mixtures.put(new Mixture("Pils", 500, 0.048, "pils").toString());
                mixtures.put(new Mixture("Wein", 200, 0.10, "wine").toString());
                mixtures.put(new Mixture("Wodka", 20, 0.40, "vodka").toString());
                mixtures.put(new Mixture("Whisky", 20, 0.40, "whisky").toString());
                mixtures.put(new Mixture("Sekt", 200, 0.12, "sparklingwine").toString());

                if (currentUser.getName().compareTo("Franzi") == 0) {
                    mixtures.put(new Mixture("Eigenes\nGetränk", 0, 0, "custom_franzi").toString());
                } else {
                    mixtures.put(new Mixture("Eigenes\nGetränk", 0, 0, "custom").toString());
                }

                editor.putString("drinks", mixtures.toString());
                editor.commit();
            }

            final Mixture[] mixtureArray = new Mixture[mixtures.length()];

            for (int i = 0; i < mixtures.length(); i++) {
                mixtureArray[i] = new Mixture(new JSONObject(mixtures.get(i).toString()));
            }

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_add_drink);

            dialog.setTitle(R.string.add_drink);
            final TextView title = (TextView) dialog.findViewById(android.R.id.title);
            if (title != null) {
                title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                title.setPadding(0, 8, 0, 6);
            }

            final MixtureAdapter mixtureAdapter = new MixtureAdapter(this, new ArrayList<Mixture>());
            GridView mixtureList = (GridView) dialog.findViewById(R.id.mixtureList);
            mixtureList.setAdapter(mixtureAdapter);

            for (Mixture aMixtureArray : mixtureArray) {
                mixtureAdapter.add(aMixtureArray);
            }

            mixtureList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    try {
                        SharedPreferences sharedPref = getSharedPreferences("data", 0);
                        SharedPreferences.Editor editor = sharedPref.edit();

                        if (mixtureArray[position].getImage().compareTo("custom") == 0 || mixtureArray[position].getImage().compareTo("custom_franzi") == 0) {
                            addCustomDrink(0, null);
                            dialog.dismiss();
                            return;
                        }
                        JSONArray mixtures = new JSONArray(sharedPref.getString("mixturesToUser", "[]"));

                        //0 = user, 1 = mixture
                        JSONArray toPut = new JSONArray();
                        toPut.put(System.currentTimeMillis());
                        toPut.put(currentUser.toString());
                        toPut.put(mixtureArray[position].toString()); //selected mixture
                        mixtures.put(toPut);

                        editor.putString("mixturesToUser", mixtures.toString());
                        editor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    dialog.dismiss();
                    addDrinks();
                }
            });
            dialog.show();


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param stage         0 = from "add drinks" dialog; 1 = from "add custom drink" dialog
     * @param customMixture from "add custom drink" dialog
     */
    public void addCustomDrink(int stage, Mixture customMixture) {
        switch (stage) {
            case 0:
                //Open dialog and wait for result
                final Dialog d = new Dialog(this);
                d.setContentView(R.layout.dialog_add_custom_drink);

                ImageView image = (ImageView) d.findViewById(R.id.image);
                if (currentUser.getName().compareTo("Franzi") == 0) {
                    image.setImageResource(getResources().getIdentifier("custom_franzi", "drawable",
                            getApplicationContext().getPackageName()));
                }

                Button addDrink = (Button) d.findViewById(R.id.addDrink);
                addDrink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView tvName = (TextView) d.findViewById(R.id.name);
                        TextView tvAmount = (TextView) d.findViewById(R.id.amount);
                        TextView tvPercentage = (TextView) d.findViewById(R.id.percentage);
                        String name = tvName.getText().toString();
                        double amount = Double.parseDouble("0" + tvAmount.getText());
                        double percentage = Double.parseDouble("0" + tvPercentage.getText()) / 100;

                        if (name.length() > 2 && amount > 1 && amount < 2000 && percentage > 0.01 && percentage < 0.95) {
                            if (currentUser.getName().compareTo("Franzi") == 0) {
                                addCustomDrink(1, new Mixture(name, amount, percentage, "custom_franzi"));
                            } else {
                                addCustomDrink(1, new Mixture(name, amount, percentage, "custom"));
                            }
                            d.dismiss();
                        } else {
                            Toast.makeText(c, c.getResources().getString(R.string.wronginput), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                d.show();
                break;
            case 1:
                try {
                    if (customMixture != null || currentUser != null) {
                        SharedPreferences sharedPref = getSharedPreferences("data", 0);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        JSONArray mixtures = new JSONArray(sharedPref.getString("mixturesToUser", "[]"));

                        //0 = user, 1 = mixture
                        JSONArray toPut = new JSONArray();
                        toPut.put(System.currentTimeMillis());
                        toPut.put(currentUser.toString());
                        toPut.put(customMixture.toString()); //selected mixture
                        mixtures.put(toPut);

                        editor.putString("mixturesToUser", mixtures.toString());
                        editor.commit();
                        addDrinks();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * Adds drinks from the current person to the list view
     */
    public void addDrinks() {
        SharedPreferences sharedPref = getSharedPreferences("data", 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        dA.clear();
        dA.notifyDataSetChanged();

        try {
            JSONArray mixtures = new JSONArray(sharedPref.getString("mixturesToUser", "[]"));

            if (mixtures.length() > 0) {
                for (int i = 0; i < mixtures.length(); i++) {

                    //0 = timestamp, 1 = user, 2 = mixture
                    JSONArray j = new JSONArray(mixtures.get(i).toString());
                    User u = new User(new JSONObject(j.get(1).toString()));

                    //Have to do this, because jsonobject == jsonobject is always false
                    if (u.toString().compareTo(currentUser.toString()) == 0) {
                        Drink d = new Drink(u, new Mixture(new
                                JSONObject(j.get(2).toString())), Long.valueOf(j.get(0).toString()));

                        dA.add(d);
                        //Timer
                        //dA.remove(d);
                    }
                }
            }

            //Clean up
            editor.putString("mixturesToUser", mixtures.toString());
            editor.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Layout-Stuff
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switchUser:
                switchUser(false);
                break;
            case R.id.editUser:
                editUser();
                break;
            case R.id.removeUser:
                try {
                    System.out.println(currentUser.toString());
                    removeUser(currentUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.newUser:
                createUser();
                break;
            case R.id.about:
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_about);

                //Add dialog title and center it
                dialog.setTitle(R.string.about);
                final TextView title = (TextView) dialog.findViewById(android.R.id.title);
                title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                title.setPadding(0, 8, 0, 6);

                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_back_again_to_leave, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1700);
    }
}

