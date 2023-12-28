package com.sai.rediclone.service;

import com.sai.rediclone.dto.RegisterRequest;
import com.sai.rediclone.exceptions.SpringRedditException;
import com.sai.rediclone.model.NotificationEmail;
import com.sai.rediclone.model.User;
import com.sai.rediclone.model.VerificationToken;
import com.sai.rediclone.repository.UserRepository;
import com.sai.rediclone.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class AuthService  {


   // private final PasswordEncoder passwordEncoder;
    private  final  UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;

    @Transactional 
    public  void signup(RegisterRequest registerRequest){
        User user = new User ();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        //user.setPassword(passwordEncoder.encode(registerRequest.getPassword())) ;
        user.setPassword(registerRequest.getPassword()) ;
        user.setCreated(Instant.now());
        user.setEnabled(false);
        userRepository.save(user);
        
       String token= generateVerificationToken(user);
        mailService.sendMail(new NotificationEmail("Please Activate your Account",
                user.getEmail(), "Thank you for signing up to Spring Reddit, " +
                "please click on the below url to activate your account : " +
                "http://localhost:8080/api/auth/accountVerification/" + token));
    }




    private  String   generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken =new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user) ;
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    public void verifyAccount(String token) {
       Optional<VerificationToken> verificationToken=verificationTokenRepository.findByToken  (token);
       verificationToken.orElseThrow( () -> new SpringRedditException("Invaild Token"));
       fetchUserAndEnable(verificationToken.get());
    }
    @Transactional
    private void fetchUserAndEnable(VerificationToken verificationToken) {
         String username= verificationToken.getUser().getUsername();
         User user   = userRepository.findByUsername(username).orElseThrow( () -> new SpringRedditException("user not found  with name " + username));
         user.setEnabled(true);
         userRepository.save(user);
    }
}
