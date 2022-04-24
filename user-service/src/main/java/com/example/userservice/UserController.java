package com.example.userservice;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UsersService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    private Environment env;
    private Greeting greeting;
    private UsersService usersService;


    public UserController(Environment env, Greeting greeting, UsersService usersService) {
        this.env = env;
        this.greeting = greeting;
        this.usersService = usersService;
    }

    @GetMapping("/health_check")
    public String status() {
        return String.format("It is working User service Port Ip : %s, %s, %s, %s", env.getProperty("local.server.port")
        ,env.getProperty("token.expiration_time"), env.getProperty("token.secret"));
    }

    @GetMapping("/welcome")
    public String welcome() {
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);
        usersService.createUser(userDto);

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

        return new ResponseEntity<>(responseUser, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> userByAll = usersService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();

        for (UserEntity userEntity : userByAll) {
            result.add(new ModelMapper().map(userEntity, ResponseUser.class));
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId) {
        UserDto userByUserId = usersService.getUserByUserId(userId);

        ResponseUser responseUser = new ModelMapper().map(userByUserId, ResponseUser.class);

        return new ResponseEntity<>(responseUser, HttpStatus.OK);
    }
}
