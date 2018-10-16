package com.csim.scu.aibox.network;



public class Url {

    //public final static String baseUrl = "http://172.20.10.9:5000/api";
    public final static String baseUrl = "http://192.168.100.105:5000/api";
    public final static String loginUrl = "/androidUser/login";
    public final static String logoutUrl = "/androidUser/logout";
    public final static String userProfile = "/androidUser/getProfile";
    public final static String userHealth = "/androidUser/getHealth";
    public final static String userNeed = "/androidUser/getNeed";
    public final static String userConversation = "/androidUser/getConversation";
    public final static String userReminder = "/androidUser/getRemind";
    public final static String nonUserReminder = "/android/getRemind";
    public final static String addUserReminder = "/androidUser/addRemind";
    public final static String getWeather = "/android/getWeather?city=";
    public final static String chatbotResponse = "/chatbot";
    public final static String userConcernLock = "/androidUser/concernLock";
    public final static String userConcernRelease = "/androidUser/concernRelease";
    public final static String dailyConcern = "/androidUser/dailyConcern";
    public final static String getECP = "/androidUser/getECP";
    public final static String setECP = "/androidUser/setECP";
    public final static String delECP = "/androidUser/deleteECP";
    public final static String getECPphone = "/android/getECPhone";
    public final static String lastLocation = "/android/getLastLocation";
    public final static String getActivity = "/android/getActivity";
    public final static String hospital = "/android/getHospital?hospital=";

    public final static String googleDirectionAPi = "https://maps.googleapis.com/maps/api/directions/json?";
}
