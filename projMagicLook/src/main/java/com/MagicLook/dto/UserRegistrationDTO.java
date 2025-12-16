package com.MagicLook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegistrationDTO {
    
    @NotBlank(message = "Username é obrigatório")
    private String username;
    
    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    private String email;
    
    @NotBlank(message = "Password é obrigatória")
    @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
    private String password;
    
    @NotBlank(message = "Primeiro nome é obrigatório")
    private String firstName;
    
    @NotBlank(message = "Último nome é obrigatório")
    private String lastName;
    
    private String telephone;
    
    public UserRegistrationDTO() {}
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
}