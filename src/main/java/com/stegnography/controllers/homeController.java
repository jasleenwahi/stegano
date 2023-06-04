package com.stegnography.controllers;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.stegnography.Repository.UserRepository;
import com.stegnography.domain.UserDetails;
import com.stegnography.domain.images;
import com.stegnography.imageService.imageServiceimpl.imageInImageStego.ImageHider;
import com.stegnography.utility.MailConstructImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.stegnography.imageService.imageService;
import com.stegnography.utility.MailConstructor;

@Slf4j
@Controller
public class homeController {

	@Autowired
	private imageService imageservice;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private MailConstructImage mailconstructer;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ImageHider imageHiderHandler;


	//	@Autowired
//	private PasswordEncoder passwordEncoder;
	String IMAGE_PREFIX = "http://www.inspireglobaleducation.com/images/";

	@RequestMapping(value = "/user")
	public String signup() {
		return "login";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String home(@ModelAttribute UserDetails user) {
		try {
//			if(userRepository.findEmailByPhoneNumber(user.getPhone_no()) == user.getEmail()){
//				System.out.println("Email Already Exists");
			String hashPassword = new BCryptPasswordEncoder().encode(user.getPassword());
			user.setPassword(hashPassword);
//			user.setPassword(passwordEncoder.encode(user.getPassword()));
			userRepository.save(user);
//			}

		} catch (DataIntegrityViolationException e) {
			throw new RuntimeException("Email already exists");
		}

		return "userlogin";
	}

	@RequestMapping(value = "/userlogin")
	public String login(Model model) {
		log.info("Inside login");
		return "userlogin";
	}

	@RequestMapping(value = "/")
	public String index() {
		log.info("Inside index");
		return "index";
	}

	@RequestMapping(value = "/authenticateuser", method = RequestMethod.POST)
	public String loginUser(@ModelAttribute UserDetails user) {
		log.info("Inside aunthentication");
		UserDetails user1 = userRepository.findPasswordByEmail(user.getEmail());
		log.info(user1.getPassword());
		boolean passwordMatches = new BCryptPasswordEncoder().matches(user.getPassword(), user1.getPassword());
		if (passwordMatches){
			log.info("Inside aunthenticatio1");
		    return "index";}
		else
			return "user";
	}

	@RequestMapping("/Imageinimages")
	public String textInImage(ModelMap model) {
		images image = new images();
		model.addAttribute("Image", image);
		images image2 = new images();
		model.addAttribute("Image", image2);
		return "image";
	}

	@GetMapping("/imageencode_confirmation")
	public String confirm(
			@RequestParam("email") String email,
			ModelMap model
	) {

		model.addAttribute("email", email);
		return "audioenc_confermation";
	}

	@PostMapping("/imageencode")
	public String audioenc(
			@RequestParam("image") MultipartFile image,
			@RequestParam("image1") MultipartFile image2,
			@RequestParam String email,
			HttpServletRequest request,
			ModelMap model
	) {

		images img = new images();
		img.setImage(image);
		imageservice.save(img);
		images img1 = new images();
		img1.setImage(image2);
		imageservice.save(img1);
		String encodedString;

		String userid = img.getId().toString();
		System.out.println("User id is: "+userid);
		try {
			File coverimage = imageservice.convert(image);
			File secretimage = imageservice.convert(image2);

//            encodedString = textInsideImage.embedText(bytes,userid, message);
			encodedString = imageHiderHandler.hide(String.valueOf(coverimage), String.valueOf(secretimage), "image_image_out.png");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		String userId = img.getId().toString();
		model.addAttribute("email",email);
		mailSender.send(mailconstructer.constructOrderConfirmationEmail1(userId, "1234", email));
		return "redirect:/imageencode_confirmation?email=" + email;
	}

	@GetMapping("/imageinimagedecode")
	public String decodeaudio(
			@RequestParam("id") String userid,
			ModelMap model
	)
	{
		model.addAttribute("id",userid);
		return "decode_image";
	}
	@PostMapping("/decode_image")
	public String decode(
			@RequestParam("user_id") String User_id,
			@RequestParam("password") String pass,
			ModelMap model
	) throws IOException
	{
//        File file2 = new File("src/main/resources/static/image/" + User_id + ".jpg");
//        BufferedImage img = ImageIO.read(file2);
		imageHiderHandler.reveal( "image_image_out.png", "founded_secret_image.png");

		return "decoded_msg_viewer_for_image";
	}
}