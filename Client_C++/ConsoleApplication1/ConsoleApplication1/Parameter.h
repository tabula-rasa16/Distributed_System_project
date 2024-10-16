#ifndef PARAMETER_H
#define PARAMETER_H

#include <string>
#include <vector>
#include <stdexcept> 
#include "ParameterType.h"



struct Parameter {
    ParameterType type;
    union {
        int intValue;
        std::string* stringValue; 
        float floatValue;         

    };

    
    Parameter(int value) : type(INTEGER), intValue(value) {}
    Parameter(const std::string& value) : type(STRING), stringValue(new std::string(value)) {}
    Parameter(float value) : type(MYFLOAT), floatValue(value) {}

    
    Parameter(const Parameter& other) : type(other.type) {
        switch (type) {
        case INTEGER:
            intValue = other.intValue;
            break;
        case STRING:
            stringValue = new std::string(*other.stringValue);
            break;
        case MYFLOAT:
            floatValue = other.floatValue;
            break;
        default:
            throw std::runtime_error("Unsupported ParameterType in copy constructor.");
        }
    }

    
    ~Parameter() {
        if (type == STRING) {
            delete stringValue;
        }
    }
}; 

#endif // PARAMETER_H
