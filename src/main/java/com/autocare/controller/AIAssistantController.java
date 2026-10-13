package com.autocare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIAssistantController {

    @PostMapping("/ask")
    public ResponseEntity<?> askAI(@RequestBody Map<String, Object> request) {
        String userQuery = ((String) request.getOrDefault("query", "")).toLowerCase();
        Map<String, Object> vehicleContext = (Map<String, Object>) request.get("vehicleContext");
        
        String responseText;
        boolean requiresMechanic = false;

        // DIY vs. Pro Enthusiast Triage Engine
        String diyRating = "🟢 DIY Friendly";
        String estimatedSavings = "₹500 - ₹1,500";
        String toolsRequired = "Basic Hand Tools";

        // If the user is asking a general maintenance question and we have vehicle context
        if (vehicleContext != null && (userQuery.contains("service") || userQuery.contains("now") || userQuery.contains("due") || userQuery.contains("check"))) {
            String make = (String) vehicleContext.get("make");
            String model = (String) vehicleContext.get("model");
            Integer mileage = (Integer) vehicleContext.get("mileage");
            
            if (mileage != null) {
                int nextOil = 10000 - (mileage % 10000);
                int nextBrake = 15000 - (mileage % 15000);
                int nextAir = 25000 - (mileage % 25000);
                
                responseText = String.format("### 🚗 Enthusiast Telemetry Analysis for %s %s (%,d km)\n\n", make, model, mileage);
                responseText += "Here is your component wear status and DIY triage:\n\n";
                
                if (nextOil <= 1000) responseText += "🔴 **Engine Oil & Filter:** Due immediately! *(DIY Level: Intermediate | Save ~₹1,200 | Time: 45m)*\n";
                else if (nextOil <= 3000) responseText += "🟡 **Engine Oil:** Approaching in " + nextOil + " km.\n";
                else responseText += "🟢 **Engine Oil:** Healthy.\n";
                
                if (nextAir <= 1500) responseText += "🟡 **Engine Air Filter:** Due soon (in " + nextAir + " km). *(DIY Level: Beginner | Save ~₹500 | Time: 10m)*\n";
                else responseText += "🟢 **Air Filter:** Healthy.\n";

                if (nextBrake <= 1000) {
                    responseText += "🔴 **Brake System:** Critical inspection due! *(Requires hydraulic bleed/pad thickness gauge)*\n";
                    requiresMechanic = true;
                    diyRating = "🔴 Pro Mechanic Recommended";
                }
                else if (nextBrake <= 3000) responseText += "🟡 **Brake Pads:** Inspect thickness soon (in " + nextBrake + " km).\n";
                else responseText += "🟢 **Brake Pads:** Healthy.\n";
                
                responseText += "\n💡 **Enthusiast Tip:** Check out our interactive **DIY Guides** in the top menu to walk through changing your air filter or oil step-by-step!";
            } else {
                responseText = "I see your " + make + " " + model + ", but I need its current mileage to predict component wear.";
            }
        } 
        // Specific DIY Repair Questions
        else if (userQuery.contains("oil") || userQuery.contains("oil change")) {
            responseText = "### 🛠️ DIY Oil Change Guide & Triage\n\n"
                    + "**Feasibility:** 🟡 Intermediate DIY (45 mins)\n"
                    + "**DIY Savings:** ~₹1,500 vs. Dealership Labor\n"
                    + "**Tools Needed:** Jack & Jack stands, oil drain pan, 14mm/17mm socket, oil filter wrench, funnel.\n\n"
                    + "**Enthusiast Steps:**\n"
                    + "1. Warm up engine for 3 mins, then elevate front onto jack stands safely.\n"
                    + "2. Unbolt the oil pan drain bolt into a drain pan. Replace crush washer.\n"
                    + "3. Unscrew old oil filter; lubricate the rubber gasket of new filter with fresh oil before hand-tightening.\n"
                    + "4. Pour correct grade synthetic oil (e.g. 5W-30) and verify dipstick level.\n\n"
                    + "👉 Visit the **DIY Guides** tab in the top navigation for the full interactive checklist!";
            diyRating = "🟡 Intermediate DIY";
            estimatedSavings = "₹1,500";
        } else if (userQuery.contains("air filter") || userQuery.contains("cabin")) {
            responseText = "### 🛠️ Air & Cabin Filter Replacement\n\n"
                    + "**Feasibility:** 🟢 100% Beginner Friendly (10-15 mins)\n"
                    + "**DIY Savings:** ~₹600 - ₹900\n"
                    + "**Tools Needed:** None or a flathead screwdriver.\n\n"
                    + "**Enthusiast Steps:**\n"
                    + "1. Engine Filter: Pop open the 3 clips on the engine airbox, pull old filter, wipe box clean, drop new filter in.\n"
                    + "2. Cabin Filter: Lower your passenger glovebox by squeezing side tabs, slide out the tray, and replace the pleated filter matching airflow arrow.\n\n"
                    + "👉 You can follow our interactive checklist under the **DIY Guides** tab!";
            diyRating = "🟢 100% Beginner Friendly";
            estimatedSavings = "₹750";
        } else if (userQuery.contains("battery") || userQuery.contains("jump") || userQuery.contains("terminal")) {
            responseText = "### ⚡ Battery Maintenance & Replacement\n\n"
                    + "**Feasibility:** 🟢 Beginner Friendly (20 mins)\n"
                    + "**DIY Savings:** ~₹800 labor savings\n"
                    + "**Tools Needed:** 10mm wrench, wire brush, baking soda solution, gloves.\n\n"
                    + "⚠️ **Safety Rule #1:** ALWAYS disconnect the **Negative (-)** terminal first, and reconnect it **LAST** to prevent hazardous electrical short circuits!\n"
                    + "Clean terminal posts with baking soda paste to dissolve acid corrosion, apply petroleum jelly, and torque clamps firmly.";
            diyRating = "🟢 Beginner Friendly";
            estimatedSavings = "₹800";
        } else if (userQuery.contains("spark") || userQuery.contains("plug") || userQuery.contains("misfire")) {
            responseText = "### ⚡ Spark Plug Inspection & Replacement\n\n"
                    + "**Feasibility:** 🟡 Intermediate DIY (40 mins)\n"
                    + "**DIY Savings:** ~₹1,200 - ₹1,800\n"
                    + "**Tools Needed:** 5/8\" or 14mm rubber-booted spark plug socket, ratchet extension, torque wrench.\n\n"
                    + "**Pro Tip:** Always work on a cold aluminum engine head to prevent stripping threads. Torque plugs to 18-22 Nm. Check out the **DIY Guides** tab for details!";
            diyRating = "🟡 Intermediate DIY";
            estimatedSavings = "₹1,500";
        } else if (userQuery.contains("brake") || userQuery.contains("noise") || userQuery.contains("squeal") || userQuery.contains("fluid")) {
            responseText = "### 🛑 Brake System Diagnosis\n\n"
                    + "**Feasibility:** 🔴 Pro Mechanic Recommended (Safety Critical)\n"
                    + "**Symptom Analysis:** High-pitched squeal indicates brake pad wear indicators are rubbing against the rotor. You have under 3mm of friction material remaining.\n\n"
                    + "⚠️ **Safety Warning:** Brakes are your vehicle's single most critical safety system. While pad inspection is DIY-friendly (see our DIY Guides), hydraulic bleeding and caliper rebuilding should be handled by a certified mechanic.\n\n"
                    + "👉 Click **Service Bookings** above to have a certified technician inspect your brakes!";
            requiresMechanic = true;
            diyRating = "🔴 Pro Mechanic Recommended";
        } else if (userQuery.contains("engine") || userQuery.contains("smoke") || userQuery.contains("transmission") || userQuery.contains("gearbox") || userQuery.contains("leak")) {
            responseText = "### 🚨 Severe Mechanical Issue Alert\n\n"
                    + "**Feasibility:** 🔴 Pro Mechanic Mandatory\n"
                    + "**Triage:** Internal engine smoke, transmission slip, or heavy fluid leaks indicate high internal pressure or mechanical wear. Attempting DIY repair without engine hoists and precision gauges can total the drivetrain.\n\n"
                    + "👉 Please book an appointment with our platform mechanics immediately under **Service Bookings**.";
            requiresMechanic = true;
            diyRating = "🔴 Pro Mechanic Mandatory";
        } else {
            responseText = "### 🏎️ AutoCare Enthusiast AI Assistant\n\n"
                    + "I help car owners and enthusiasts diagnose issues, learn how to wrench on their cars, and triage repairs!\n\n"
                    + "**Try asking me:**\n"
                    + "* \"How do I do an oil change myself?\"\n"
                    + "* \"What service is due on my car right now?\"\n"
                    + "* \"How do I replace my air filter and how much will I save?\"\n"
                    + "* \"Why is my engine squeaking or idling rough?\"\n\n"
                    + "Or explore our interactive step-by-step checklists in the **DIY Guides** section above!";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("answer", responseText);
        response.put("requiresMechanic", requiresMechanic);
        response.put("diyRating", diyRating);
        response.put("estimatedSavings", estimatedSavings);
        
        return ResponseEntity.ok(response);
    }
}
