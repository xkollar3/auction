package edu.fi.muni.cz.marketplace.user.dto;

import lombok.Value;

@Value
public class Address {

  String line1;
  String line2;
  String city;
  String state;
  String postalCode;
  String country;
}
