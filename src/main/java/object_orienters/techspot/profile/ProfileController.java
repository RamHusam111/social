package object_orienters.techspot.profile;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/profiles")
public class ProfileController {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final ProfileModelAssembler assembler;
    private final ImpleProfileService profileService;
    private final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    public ProfileController(ProfileModelAssembler assembler, ImpleProfileService profileService) {
        this.assembler = assembler;
        this.profileService = profileService;
    }

    private static String getTimestamp() {
        return LocalDateTime.now().format(formatter) + " ";
    }

    // get user profile
    @GetMapping("/{username}")
    public ResponseEntity<?> one(@PathVariable String username) {
        try {
            logger.info(
                    ">>>>Retrieving Profile From Database... " + getTimestamp() + "<<<<");
            EntityModel<Profile> profileModel = assembler.toModel(profileService.getUserByUsername(username));
            logger.info(">>>>Profile  Retrieved. " + getTimestamp() + "<<<<");
            return ResponseEntity.ok(profileModel);
        } catch (UserNotFoundException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("User Not Found").withDetail(exception.getMessage()));
        }
    }

    ;

    // update user profile
    @PutMapping("/{username}")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody Profile newUser, @PathVariable String username) {
        try {
            logger.info(">>>>Updating Profile... " + getTimestamp() + "<<<<");
            Profile updatedUser = profileService.updateUserProfile(newUser, username);
            logger.info(">>>>Profile Updated. " + getTimestamp() + "<<<<");
            return ResponseEntity.ok()
                    .location(assembler.toModel(updatedUser).getRequiredLink(IanaLinkRelations.SELF).toUri())
                    .body(assembler.toModel(updatedUser));
        } catch (UserNotFoundException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("User Not Found").withDetail(exception.getMessage()));
        }
    }

    // get user followers
    @GetMapping("/{username}/followers")
    public ResponseEntity<?> Followers(@PathVariable String username) {
        try {
            logger.info(">>>>Retrieving Followers List... " + getTimestamp() + "<<<<");
            List<EntityModel<Profile>> followers = profileService.getUserFollowersByUsername(username).stream()
                    .map(assembler::toModel)
                    .collect(Collectors.toList());
            logger.info(">>>>Followers List Retrieved. " + getTimestamp() + "<<<<");
            return ResponseEntity.ok(CollectionModel.of(followers,
                    linkTo(methodOn(ProfileController.class).one(username)).withSelfRel()));
        } catch (UserNotFoundException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("User Not Found").withDetail(exception.getMessage()));
        }
    }

    // get specific user follower
    @GetMapping("/{username}/followers/{followerUserName}")
    public ResponseEntity<?> getSpecificFollower(@PathVariable String username,
            @PathVariable String followerUserName) {
        try {
            logger.info(">>>>Retrieving Follower... " + getTimestamp() + "<<<<");
            Profile follower = profileService.getFollowerByUsername(username, followerUserName);
            logger.info(">>>>Follower Retrieved. " + getTimestamp() + "<<<<");
            return ResponseEntity.ok(assembler.toModel(follower));
        } catch (UserNotFoundException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("User Not Found").withDetail(exception.getMessage()));
        }
    }

    // get user followers
    @GetMapping("/{username}/following")
    public ResponseEntity<?> Following(@PathVariable String username) {
        try {
            logger.info(">>>>Retrieving Following List... " + getTimestamp() + "<<<<");
            List<EntityModel<Profile>> following = profileService.getUserFollowingByUsername(username).stream()
                    .map(userModel -> assembler.toModel(userModel))
                    .collect(Collectors.toList());
            logger.info(">>>>Following List Retrieved. " + getTimestamp() + "<<<<");
            return ResponseEntity.ok(CollectionModel.of(following,
                    linkTo(methodOn(ProfileController.class).one(username)).withSelfRel()));
        } catch (UserNotFoundException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("User Not Found").withDetail(exception.getMessage()));
        }
    }

    // get specific user following
    @GetMapping("/{username}/following/{followingUsername}")
    public ResponseEntity<?> getSpecificFollowing(@PathVariable String username,
            @PathVariable String followingUsername) {
        try {
            logger.info(">>>>Retrieving Following Profile... " + getTimestamp() + "<<<<");
            Profile followingProfile = profileService.getFollowingByUsername(username, followingUsername)
                    .orElseThrow(() -> new UserNotFoundException(followingUsername));
            logger.info(">>>>Following Profile Retrieved. " + getTimestamp() + "<<<<");
            return ResponseEntity
                    .ok(assembler.toModel(followingProfile));
        } catch (UserNotFoundException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("User Not Found").withDetail(exception.getMessage()));
        }
    }

    // add new follower to user
    @PostMapping("/{username}/followers")
    @PreAuthorize("#followerUserName.get(\"username\").asText() == authentication.principal.username")
    public ResponseEntity<?> newFollower(@PathVariable String username, @RequestBody ObjectNode followerUserName) {
        try {
            logger.info(">>>>Adding New Follower... " + getTimestamp() + "<<<<");
            Profile newFollower = profileService.addNewFollower(username, followerUserName.get("username").asText());
            logger.info(">>>>New Follower Added. " + getTimestamp() + "<<<<");
            return ResponseEntity.ok(assembler.toModel(newFollower));
        } catch (UserNotFoundException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("User Not Found").withDetail(exception.getMessage()));
        } catch (UserCannotFollowSelfException ex) {
            logger.info(">>>>Error Occurred: " + ex.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Problem.create().withTitle("Bad Request").withDetail(ex.getMessage()));

        }
    }

    // delete follower from user
    @DeleteMapping("/{username}/followers")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<?> deleteFollower(@PathVariable String username, @RequestBody ObjectNode deletedUser) {
        try {
            logger.info(">>>>Deleting Follower... " + getTimestamp() + "<<<<");
            profileService.deleteFollower(username, deletedUser.get("deletedUser").asText());
            logger.info(">>>>Follower Added. " + getTimestamp() + "<<<<");
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("User Not Found").withDetail(exception.getMessage()));
        }
    }

    @DeleteMapping("/{username}/following")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<?> deleteFollowing(@PathVariable String username, @RequestBody ObjectNode deletedUser) {
        try {
            logger.info(">>>>Deleting Following... " + getTimestamp() + "<<<<");
            profileService.deleteFollowing(username, deletedUser.get("deletedUser").asText());
            logger.info(">>>>Following Added. " + getTimestamp() + "<<<<");
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("User Not Found").withDetail(exception.getMessage()));
        }
    }

    @PostMapping("/{username}/profilePic")
    public ResponseEntity<?> addProfilePic(@PathVariable String username,
            @RequestParam(value = "file") MultipartFile file,
            @RequestParam(value = "text", required = false) String text) throws UserNotFoundException, IOException {
        try {
            logger.info(">>>>Adding Profile Picture... " + getTimestamp() + "<<<<");
            Profile profile = profileService.addProfilePic(username, file, text);
            logger.info(">>>>Profile Picture Added. " + getTimestamp() + "<<<<");
            return ResponseEntity.ok(assembler.toModel(profile));
        } catch (UserNotFoundException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("User Not Found").withDetail(exception.getMessage()));
        } catch (IOException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("File Not Found").withDetail(exception.getMessage()));

        }
    }

    @DeleteMapping("/{username}/delete")
    public ResponseEntity<?> deleteProfile(@PathVariable String username) throws UserNotFoundException {
        try {
            profileService.deleteProfile(username);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException exception) {
            logger.info(">>>>Error Occurred:  " + exception.getMessage() + " " + getTimestamp() + "<<<<");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Problem.create().withTitle("User Not Found").withDetail(exception.getMessage()));
        }
    }

}
