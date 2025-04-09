-- Add more topics to the electronics unit and populate with diverse questions
USE kenyatta_cat_system;

-- Add new topics for electronics (SCO100)
INSERT INTO topics (topic_id, topic_name, description, unit_code, created_by)
VALUES 
(UUID(), 'Analog Electronics', 'Study of analog circuits and components including amplifiers, filters, and signal processing', 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07'),
(UUID(), 'Power Electronics', 'Study of switching devices, power supplies, inverters, and motor control circuits', 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07'),
(UUID(), 'Microcontrollers', 'Study of microcontroller architecture, programming, and applications', 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07'),
(UUID(), 'Electronic Measurements', 'Study of measurement techniques, instruments, and error analysis in electronics', 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07'),
(UUID(), 'Communication Systems', 'Study of electronic communication systems, modulation techniques, and protocols', 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07');

-- Get topic IDs for reference (these would be replaced with actual UUIDs in a real implementation)
-- SET @analog_electronics_id = (SELECT topic_id FROM topics WHERE topic_name = 'Analog Electronics' AND unit_code = 'SCO100' LIMIT 1);
-- SET @power_electronics_id = (SELECT topic_id FROM topics WHERE topic_name = 'Power Electronics' AND unit_code = 'SCO100' LIMIT 1);
-- SET @microcontrollers_id = (SELECT topic_id FROM topics WHERE topic_name = 'Microcontrollers' AND unit_code = 'SCO100' LIMIT 1);
-- SET @electronic_measurements_id = (SELECT topic_id FROM topics WHERE topic_name = 'Electronic Measurements' AND unit_code = 'SCO100' LIMIT 1);
-- SET @communication_systems_id = (SELECT topic_id FROM topics WHERE topic_name = 'Communication Systems' AND unit_code = 'SCO100' LIMIT 1);

-- 1. Analog Electronics (Multiple Choice Questions)
-- Easy
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'What is the function of an operational amplifier?', 'MULTIPLE_CHOICE', 'To amplify digital signals,To amplify analog signals,To convert analog to digital,To store data', 'To amplify analog signals', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Analog Electronics', 'EASY'),
(UUID(), 'Which component is used to block DC and pass AC signals?', 'MULTIPLE_CHOICE', 'Resistor,Capacitor,Inductor,Diode', 'Capacitor', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Analog Electronics', 'EASY');

-- Medium
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'What is the gain of an inverting amplifier with a 10kΩ feedback resistor and a 2kΩ input resistor?', 'MULTIPLE_CHOICE', '-5,-2,5,2', '-5', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Analog Electronics', 'MEDIUM'),
(UUID(), 'Which of the following is a characteristic of a Class A amplifier?', 'MULTIPLE_CHOICE', 'High efficiency,Low distortion,Operates for 180 degrees of the input cycle,Requires two transistors', 'Low distortion', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Analog Electronics', 'MEDIUM');

-- Hard
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'In a differential amplifier, what is the common-mode rejection ratio (CMRR) used to measure?', 'MULTIPLE_CHOICE', 'The ability to amplify differential signals,The ability to reject common-mode signals,The ratio of input to output impedance,The frequency response of the amplifier', 'The ability to reject common-mode signals', 5, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Analog Electronics', 'HARD');

-- 2. Power Electronics (Multiple Choice Questions)
-- Easy
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'Which device is commonly used as a switch in power electronics?', 'MULTIPLE_CHOICE', 'Resistor,Capacitor,Transistor,Operational Amplifier', 'Transistor', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Power Electronics', 'EASY'),
(UUID(), 'What is the function of a rectifier in power electronics?', 'MULTIPLE_CHOICE', 'Convert AC to DC,Convert DC to AC,Amplify signals,Filter noise', 'Convert AC to DC', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Power Electronics', 'EASY');

-- Medium
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'Which of the following is NOT a type of power converter?', 'MULTIPLE_CHOICE', 'AC-DC,DC-AC,DC-DC,AC-RF', 'AC-RF', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Power Electronics', 'MEDIUM'),
(UUID(), 'What is the purpose of a snubber circuit in power electronics?', 'MULTIPLE_CHOICE', 'To increase switching speed,To reduce switching losses and protect against voltage spikes,To increase power output,To filter input signals', 'To reduce switching losses and protect against voltage spikes', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Power Electronics', 'MEDIUM');

-- Hard
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'In a buck converter, what happens to the output voltage compared to the input voltage?', 'MULTIPLE_CHOICE', 'Output voltage is higher than input voltage,Output voltage is lower than input voltage,Output voltage equals input voltage,Output voltage is inverted', 'Output voltage is lower than input voltage', 5, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Power Electronics', 'HARD');

-- 3. Microcontrollers (Multiple Choice Questions)
-- Easy
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'Which of the following is a popular microcontroller family?', 'MULTIPLE_CHOICE', 'ARM,Intel Core,AMD Ryzen,Nvidia GeForce', 'ARM', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Microcontrollers', 'EASY'),
(UUID(), 'What does UART stand for in microcontroller communications?', 'MULTIPLE_CHOICE', 'Universal Asynchronous Receiver Transmitter,Unified Analog Radio Transmission,Universal Analog Receiver Terminal,Unified Asynchronous Receiver Terminal', 'Universal Asynchronous Receiver Transmitter', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Microcontrollers', 'EASY');

-- Medium
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'Which of the following is NOT a common communication protocol used by microcontrollers?', 'MULTIPLE_CHOICE', 'I2C,SPI,USB,HDCP', 'HDCP', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Microcontrollers', 'MEDIUM'),
(UUID(), 'What is the purpose of a watchdog timer in a microcontroller?', 'MULTIPLE_CHOICE', 'To measure execution time of programs,To reset the microcontroller if it hangs,To synchronize with external devices,To count elapsed time', 'To reset the microcontroller if it hangs', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Microcontrollers', 'MEDIUM');

-- Hard
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'Which memory type is typically used for program storage in microcontrollers?', 'MULTIPLE_CHOICE', 'SRAM,DRAM,Flash,EEPROM', 'Flash', 5, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Microcontrollers', 'HARD');

-- 4. Electronic Measurements (Short Answer Questions)
-- Easy
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'What instrument is used to measure voltage?', 'SHORT_ANSWER', NULL, 'voltmeter', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Electronic Measurements', 'EASY'),
(UUID(), 'What is the unit of electrical resistance?', 'SHORT_ANSWER', NULL, 'ohm', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Electronic Measurements', 'EASY');

-- Medium
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'What does RMS stand for in electrical measurements?', 'SHORT_ANSWER', NULL, 'root mean square', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Electronic Measurements', 'MEDIUM'),
(UUID(), 'What instrument is used to measure frequency?', 'SHORT_ANSWER', NULL, 'frequency counter', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Electronic Measurements', 'MEDIUM');

-- Hard
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'What is the principle behind a digital storage oscilloscope?', 'SHORT_ANSWER', NULL, 'analog-to-digital conversion', 5, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Electronic Measurements', 'HARD');

-- 5. Communication Systems (List-Based Questions)
-- Easy
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'List three types of modulation techniques used in communication systems.', 'LIST_BASED', NULL, 'AM,FM,PM', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Communication Systems', 'EASY'),
(UUID(), 'List two advantages of digital communication over analog communication.', 'LIST_BASED', NULL, 'noise immunity,error correction', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Communication Systems', 'EASY');

-- Medium
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'List three components of a basic communication system.', 'LIST_BASED', NULL, 'transmitter,channel,receiver', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Communication Systems', 'MEDIUM'),
(UUID(), 'List three types of noise that can affect communication systems.', 'LIST_BASED', NULL, 'thermal noise,shot noise,interference', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Communication Systems', 'MEDIUM');

-- Hard
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'List four layers of the OSI model.', 'LIST_BASED', NULL, 'physical,data link,network,transport', 5, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Communication Systems', 'HARD');

-- Add more Short Answer questions for various topics
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'What is the SI unit of capacitance?', 'SHORT_ANSWER', NULL, 'farad', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Basic Electrical Concepts', 'EASY'),
(UUID(), 'What is the name of the law that states the total current entering a junction equals the total current leaving it?', 'SHORT_ANSWER', NULL, 'kirchhoff''s current law', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Basic Electrical Concepts', 'MEDIUM'),
(UUID(), 'What is the Boolean expression for an XOR gate with inputs A and B?', 'SHORT_ANSWER', NULL, 'A⊕B', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Logic Gates', 'MEDIUM'),
(UUID(), 'What semiconductor material has four valence electrons?', 'SHORT_ANSWER', NULL, 'silicon', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Semiconductor Devices', 'EASY'),
(UUID(), 'What is the full form of MOSFET?', 'SHORT_ANSWER', NULL, 'metal oxide semiconductor field effect transistor', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Semiconductor Devices', 'MEDIUM');

-- Add more List-Based questions for various topics
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES 
(UUID(), 'List three basic passive components used in electronic circuits.', 'LIST_BASED', NULL, 'resistor,capacitor,inductor', 2, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Basic Electrical Concepts', 'EASY'),
(UUID(), 'List three types of semiconductor devices.', 'LIST_BASED', NULL, 'diode,transistor,thyristor', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Semiconductor Devices', 'MEDIUM'),
(UUID(), 'List the four basic logic gates.', 'LIST_BASED', NULL, 'AND,OR,NOT,XOR', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Logic Gates', 'MEDIUM'),
(UUID(), 'List three types of flip-flops.', 'LIST_BASED', NULL, 'D flip-flop,JK flip-flop,T flip-flop', 3, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Digital Electronics', 'MEDIUM'),
(UUID(), 'List four binary operations used in digital systems.', 'LIST_BASED', NULL, 'AND,OR,NOT,XOR', 4, 'SCO100', '5a216c66-3e6f-45a6-8599-5bfe0fb24a07', 1, 'Digital Electronics', 'HARD');
