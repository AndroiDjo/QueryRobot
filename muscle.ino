#include <VarSpeedServo.h>
#include <NewPing.h>

// сонар
#define TRIGGER_PIN 8
#define ECHO_PIN 9
#define MAX_DISTANCE 200

#define TEMP_PIN A0 // термистор
#define LIGHT_PIN A1 // фоторезистор

// термистор
#define TERMIST_B 4300
#define VIN 5.0

int LED = 13;
String input_string = "";

// горизонтальный сервопривод
int SERV_H = 10; // пин
VarSpeedServo servh;
int servhd = 0; // градус поворота

// вертикальный сервопривод
int SERV_V = 11; // пин
VarSpeedServo servv;
int servvd = 0; // градус поворота

int SERVO_SPEED = 100; // скорость поворота серво

NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE); // Настройка пинов и максимального расстояния.

void setup()
{
  Serial.begin(9600);
  pinMode(LED, OUTPUT);
  digitalWrite(LED, HIGH);
  servh.attach(SERV_H);
  servv.attach(SERV_V);
}

void loop()
{
  while (Serial.available() > 0) {
    char c = Serial.read();
    if (c == '\n' || c == ';')
    { 
      doCommand(input_string);
      input_string = "";
    }
    else
    {
        input_string += c;
    }
  }
}

void doCommand(String command)
{
  String cmd = command;
  
  Serial.println("doCommand:"+cmd);
  
  if (cmd.equals("1"))
  {
    digitalWrite(LED, HIGH);
    Serial.println("LED is ON");
  }
  else if (cmd.equals("0"))
  {
    digitalWrite(LED, LOW);
    Serial.println("LED is OFF");
  }
  else if (cmd.startsWith("sh")) // сервопривод горизонтальный
  {
    cmd.replace("sh", "");
    servhd = getHServoDegree(cmd.toInt());
    servh.write(servhd, SERVO_SPEED);
    Serial.print("Servo set to ");
    Serial.println(servhd);
  }
  else if (cmd.startsWith("sv")) // сервопривод вертикальный
  {
    cmd.replace("sv", "");
    servvd = getVServoDegree(cmd.toInt());
    servv.write(servvd, SERVO_SPEED);
    Serial.print("Servo set to ");
    Serial.println(servvd);
  }
  else if (cmd.equals("sonar"))
  {
    unsigned int uS = sonar.ping_cm();
    Serial.println(String(uS)+"cm");
  }
  else if (cmd.equals("temp"))
  {
    float voltage = analogRead(A0) * VIN / 1023.0;
    float r1 = voltage / (VIN - voltage);
    float temperature = 1./( 1./(TERMIST_B)*log(r1)+1./(25. + 273.) ) - 273;
    Serial.println(String(temperature)+"degr");
  }
  else if (cmd.equals("light")) {
    int lightness = analogRead(LIGHT_PIN);
    Serial.println(lightness);
  }
  
}

int getHServoDegree(int d)
{
  int result = d; 
  if (d > 179) {
    result = 179;
  }
  else if (d < 20) {
    result = 20;
  }
  return result;
}

int getVServoDegree(int d)
{
  int result = d; 
  if (d > 179) {
    result = 179;
  }
  else if (d < 50) {
    result = 50;
  }
  return result;
}