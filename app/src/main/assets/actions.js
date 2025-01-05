{
  "actions": [
    {
      "name": "CALL_AMBULANCE",
      "availability": {
        "deviceClasses": [
          {
            "assistantDeviceClass": "ANDROID"
          }
        ]
      },
      "intent": {
        "name": "com.life.lifelink.CALL_AMBULANCE",
        "parameters": []
      },
      "fulfillment": {
        "staticFulfillment": {
          "templatedResponse": {
            "items": [
              {
                "simpleResponse": {
                  "textToSpeech": "Calling ambulance through LifeLink"
                }
              }
            ]
          }
        }
      }
    }
  ]
}