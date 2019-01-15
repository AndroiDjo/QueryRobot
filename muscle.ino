//include <VarSpeedServo.h>
#include <Servo.h>
#include <NewPing.h>
#include <EEPROM.h>

// сонар
#define TRIGGER_PIN 12
#define ECHO_PIN 11
#define MAX_DISTANCE 200

// моторы
#define E1 3 // Enable Pin for motor 1
#define I1 4 // Control pin 1 for motor 1
#define I2 2 // Control pin 2 for motor 1
#define E2 6 // Enable Pin for motor 2
#define I3 7 // Control pin 1 for motor 2
#define I4 8 // Control pin 2 for motor 2
int curSpeed;
int motorSpeed = 0;
int motorTime = 0;
int stopDistance = 0;
unsigned long prev_time = 0;
const int minSpeed = 90;
const int maxSpeed = 255;

#define TEMP_PIN A0 // термистор
#define LIGHT_PIN A1 // фоторезистор

// термистор
#define TERMIST_B 4300
#define VIN 5.0

int LED = 13;
String input_string = "";

// вертикальный сервопривод
int SERV_VL = 9; // левый
int SERV_VR = 10; // правый
Servo servvl;
Servo servvr;
int servvd = 0; // градус поворота

int SERVO_SPEED = 100; // скорость поворота серво

NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE); // Настройка пинов и максимального расстояния.

unsigned long prev100ms = 0;

void setup()
{
  Serial.begin(9600);
  pinMode(LED, OUTPUT);
  servvl.attach(SERV_VL);
  servvr.attach(SERV_VR);
  pinMode(E1, OUTPUT);
  pinMode(E2, OUTPUT);
  pinMode(I1, OUTPUT);
  pinMode(I2, OUTPUT);
  pinMode(I3, OUTPUT);
  pinMode(I4, OUTPUT);
  delay(100);
  servvl.write(EEPROM[60]);
  servvr.write(EEPROM[160]);
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
  
  motorEngine();
}

void doCommand(String command)
{
  String cmd = command;
  
  //Serial.println("doCommand:"+cmd);
  
  if (cmd.equals("1"))
  {
    digitalWrite(LED, HIGH);
  }
  else if (cmd.equals("0"))
  {
    digitalWrite(LED, LOW);
  }
  else if (cmd.startsWith("sv")) // сервопривод вертикальный
  {
    cmd.replace("sv", "");
    servvd = getVServoDegree(cmd.toInt());
    servvl.write(EEPROM[servvd]);
    servvr.write(EEPROM[servvd+100]);
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
  else if (cmd.startsWith("mot")) {
    int partRes = getSplitPart(cmd, '&', 1).toInt();
    if (partRes == 1) {
      digitalWrite(I1, LOW);
      digitalWrite(I2, HIGH);
    } else if (partRes == -1) {
      digitalWrite(I1, HIGH);
      digitalWrite(I2, LOW);
    } else {
      digitalWrite(I1, LOW);
      digitalWrite(I2, LOW);
    }
    
    partRes = getSplitPart(cmd, '&', 2).toInt();
    if (partRes == 1) {
      digitalWrite(I3, LOW);
      digitalWrite(I4, HIGH);
    } else if (partRes == -1) {
      digitalWrite(I3, HIGH);
      digitalWrite(I4, LOW);
    } else {
      digitalWrite(I3, LOW);
      digitalWrite(I4, LOW);
    }
    
    motorSpeed = getMotorSpeed(getSplitPart(cmd, '&', 3).toInt());
    motorTime = getSplitPart(cmd, '&', 4).toInt();
    stopDistance = getSplitPart(cmd, '&', 5).toInt();
    prev_time = millis();
  }
  
}

int getMotorSpeed(int s)
{
  int result = s; 
  if (s > maxSpeed) {
    result = maxSpeed;
  }
  else if (s < minSpeed) {
    result = minSpeed;
  }
  return result;
}

void motorEngine()
{
   if (motorTime > 0) {
     if (millis() - prev_time <= motorTime) {
       checkBarrier();
       if (curSpeed <= maxSpeed) {
         analogWrite(E1, curSpeed);
         analogWrite(E2, curSpeed);
         ++curSpeed;
       }
     } else {
       stopMotors();
     }
   }
}

void checkBarrier()
{
  if (stopDistance > 0) {
    if (millis() - prev100ms > 50) {
      prev100ms = millis();
      if (sonar.ping_cm() <= stopDistance) {
        stopMotors();
        Serial.println("brr");
      }
    }
  }
}

void stopMotors()
{
   digitalWrite(I1, LOW);
   digitalWrite(I2, LOW);
   digitalWrite(I3, LOW);
   digitalWrite(I4, LOW);
   digitalWrite(E1, LOW);
   digitalWrite(E2, LOW);
   motorTime = 0;
   curSpeed = minSpeed;
   stopDistance = 0;
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
  if (d > 99) {
    result = 99;
  }
  else if (d < 0) {
    result = 0;
  }
  return result;
}

String getSplitPart(String data, char separator, int index)
{
    int found = 0;
    int strIndex[] = { 0, -1 };
    int maxIndex = data.length() - 1;

    for (int i = 0; i <= maxIndex && found <= index; i++) {
        if (data.charAt(i) == separator || i == maxIndex) {
            found++;
            strIndex[0] = strIndex[1] + 1;
            strIndex[1] = (i == maxIndex) ? i+1 : i;
        }
    }
    
    return found > index ? data.substring(strIndex[0], strIndex[1]) : "";
}