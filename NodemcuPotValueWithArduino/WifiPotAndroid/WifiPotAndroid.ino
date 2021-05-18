#include<ESP8266WiFi.h>
#include<FirebaseArduino.h>

#define FIREBASE_HOST ""  //This came from Firebase
#define FIREBASE_AUTH "" //This came from Firebase
#define WIFI_SSID "" //Your internet id
#define WIFI_PASSWORD "" //Your internet password

#define potpin A0
int deger = 0;

void setup() {

  Serial.begin(9600);

WiFi.begin(WIFI_SSID,WIFI_PASSWORD);
Serial.println("CONNECTİNG");
while(WiFi.status() != WL_CONNECTED){
  Serial.print(".");
  delay(500);
}
Serial.println();
Serial.print("Connected:");
Serial.println(WiFi.localIP());

Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);

}

void loop() {

  deger = analogRead(potpin);
  Serial.println(deger);
  
  String veriGetir = (Firebase.getString("veriGetir"));
  Serial.print("veriGetirmeDurumu: "); Serial.println(veriGetir);
  String gonderilecekVeri = String(deger);
  if(veriGetir == "1"){
    
    Firebase.setString("PotDeger",gonderilecekVeri);
  }
  if(veriGetir == "0"){
    
    String veriYok = "Veri alınmamaktadir.";
    Firebase.setString("PotDeger",veriYok);
  }
  delay(500);
}
