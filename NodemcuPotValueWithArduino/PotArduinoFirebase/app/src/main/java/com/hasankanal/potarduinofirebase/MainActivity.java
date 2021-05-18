package com.hasankanal.potarduinofirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.SystemClock;
import android.renderscript.Element;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String veriGetir,name,gelenPotDegeri,isim;

    TextView titleText,veriText;
    EditText nameEt;
    Button veriGetirBt,veriGizleBt,addButton,clearButton;
    ListView dataList,potList;
                                                                    //Kullanilacak degiskenler tanimlandi.
    ArrayAdapter arrayAdapter,arrayAdapter2;

    ArrayList<String> nameArray;
    ArrayList<Integer> idArray;
    ArrayList<String> potDegerArray;

    SQLiteDatabase sqLiteDatabase;                               //SQLiteDatabase kullanabilmek icin.


    private DatabaseReference databaseReference;                //Firebase uzerinden veri alabilmek ve Firebase'e veri yollamak icin.
    private DatabaseReference getDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleText = findViewById(R.id.titleTv);
        veriText = findViewById(R.id.veriTv);
        nameEt = findViewById(R.id.nameEt);
        addButton = findViewById(R.id.addButton);
        clearButton = findViewById(R.id.clearButton);                       //Tanimlamalar.
        veriGetirBt = findViewById(R.id.veriGetirBt);
        veriGizleBt = findViewById(R.id.veriGizleBt);

        dataList = findViewById(R.id.dataList);              //Alinan isimleri ve pot degerlerini gosterebilmek icin gerekli ListViewlar
        potList = findViewById(R.id.potList);

        nameArray = new ArrayList<String>();
        idArray = new ArrayList<Integer>();                  //Alinan isimleri ve pot degerlerini kaydedebilmek icin gerekli ArrayListler
        potDegerArray = new ArrayList<String>();


        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,nameArray);
        dataList.setAdapter(arrayAdapter);

        arrayAdapter2 = new ArrayAdapter(this,android.R.layout.simple_list_item_1,potDegerArray);
        potList.setAdapter(arrayAdapter2);                      //ListViewlari kullanabilmemiz ve sergileyebilmemiz icin gerekli adapterlar

        databaseReference = FirebaseDatabase.getInstance().getReference();
        getDatabaseReference = FirebaseDatabase.getInstance().getReference().child("PotDeger");     //Firebase tanimlamalari.

        handleClick();              //Butonlarin setOnClikclerinin toplandigi fonksiyon

        sqLiteDatabase = getApplicationContext().openOrCreateDatabase("PotDegerleri",MODE_PRIVATE,null);   //Database olusturuldu.

        getData(); //SQLite veri tabanindan verileri okudugumuz fonksiyon.

    }

    private void handleClick() {
        veriGetirBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                veriGetir = "1";
                HashMap<String,Object> boolData = new HashMap<>();
                boolData.put("veriGetir",veriGetir);                        //Firebase'e veri yolladik.
                databaseReference.updateChildren(boolData);
                getValue();

            }
        });

        veriGizleBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                veriGetir = "0";
                HashMap<String,Object> boolData = new HashMap<>();
                boolData.put("veriGetir",veriGetir);                    //Firebase'e veri yolladik.
                databaseReference.updateChildren(boolData);
                veriText.setText("Veri Gizlendi.");
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = nameEt.getText().toString();                     //Alinan degerler degiskenlere atandi
                gelenPotDegeri = veriText.getText().toString();

                    try{
                        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS potDegerleri (id INTEGER PRIMARY KEY,  name VARCHAR, gelenPotDegeri VARCHAR)");

                        String sqlString = "INSERT INTO potDegerleri (name, gelenPotDegeri) VALUES (?,?)";

                        SQLiteStatement sqLiteStatement = sqLiteDatabase.compileStatement(sqlString);


                        sqLiteStatement.bindString(1,name);     //Veri Tabani olusturuldu ve SQLiteStatement ozelligi ile degerler SQLite veri tabanina kaydedildi.
                        sqLiteStatement.bindString(2,gelenPotDegeri);
                        sqLiteStatement.execute();



                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    nameArray.clear();      //Arraylar guncellenmek uzere temizlendi ve getData() fonksiyonu ile yeni verilerle beraber guncellendi.
                    potDegerArray.clear();
                    getData();

                    nameEt.setText("");

            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    isim = nameArray.get(nameArray.size()-1);

                    sqLiteDatabase = getApplicationContext().openOrCreateDatabase("PotDegerleri",MODE_PRIVATE,null);

                    String sqlString = "DELETE FROM potDegerleri WHERE name=(?)";       //Kayitli veriler SQLiteStatement ozelligi kullanilarak son kaydedilen veriden baslanarak silme islemi yapilir.

                    SQLiteStatement sqLiteStatement = sqLiteDatabase.compileStatement(sqlString);


                    sqLiteStatement.bindString(1,isim);
                    sqLiteStatement.execute();


                    nameArray.clear();
                    potDegerArray.clear();
                    getData();

                }catch(Exception hata){
                    Toast.makeText(getApplicationContext(),"Silinecek veri yok",Toast.LENGTH_SHORT).show();
                }



            }
        });


    }

    private void getValue() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Veri potDegeri = new Veri();
                potDegeri.setPotDegeri(snapshot.getValue().toString());
                veriText.setText("Alınan pot degeri: "+ potDegeri.getPotDegeri());      //Firebase uzerinden veri alindi ve degiskene atandi
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        getDatabaseReference.addValueEventListener(valueEventListener);
    }

    public void getData(){

       try{
           sqLiteDatabase = getApplicationContext().openOrCreateDatabase("PotDegerleri",MODE_PRIVATE,null);
           Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM potDegerleri",null);
           int nameIx = cursor.getColumnIndex("name");
           int idIx = cursor.getColumnIndex("id");
           int potDegerIx = cursor.getColumnIndex("gelenPotDegeri");        //Veri tabanina kaydedilen veriler Cursor kullanilarak Arraylere atandi.

           while(cursor.moveToNext()){
               nameArray.add(cursor.getString(nameIx));
               idArray.add(cursor.getInt(idIx));
               potDegerArray.add(cursor.getString(potDegerIx));

           }

           arrayAdapter.notifyDataSetChanged();
           arrayAdapter2.notifyDataSetChanged();

           cursor.close();

       }catch(Exception e){
           e.printStackTrace();
       }
    }
}