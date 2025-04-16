package com.example.Brawch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class BrawchApplication {
	public static void main(String[] args) {
		SpringApplication.run(BrawchApplication.class, args);
	}

	@Controller
	public static class main {
		@GetMapping("/main")
		public String main(){
			return "main";
		}
	}

}
