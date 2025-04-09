-- Populate questions for SCO100 unit topics
-- Using the following topics:
-- 1. Digital Electronics (64bf18dd-7d0c-4bbc-a168-d79c2861c52b)
-- 2. Basic Electrical Concepts (82b3c587-e03b-4fef-b83e-20476063af62)
-- 3. Logic Gates (b1602cae-7207-4f67-a6cd-23b28f88f2a4)
-- 4. Semiconductor Devices (bc5c54c2-8835-4c6a-80d7-974e16278b7a)
-- 5. Logic Design & Combinational Circuits (dcc87087-0f9c-4bae-99a4-605ca141733e)

USE kenyatta_cat_system;

-- Clear existing questions for SCO100 unit
DELETE FROM questions WHERE unit_code = 'SCO100';

-- 1. Digital Electronics (10 questions)
-- Question 1 (Multiple Choice, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which of the following is a basic unit of digital information?', 'MULTIPLE_CHOICE', 'Byte,Bit,Nibble,Word', 'Bit', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Digital Electronics', 'EASY');

-- Question 2 (Multiple Choice, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'How many bits are in a byte?', 'MULTIPLE_CHOICE', '4,8,16,32', '8', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Digital Electronics', 'EASY');

-- Question 3 (Multiple Choice, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which numbering system is commonly used in digital electronics?', 'MULTIPLE_CHOICE', 'Decimal,Binary,Octal,All of the above', 'All of the above', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Digital Electronics', 'MEDIUM');

-- Question 4 (Multiple Choice, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What is the binary representation of decimal number 15?', 'MULTIPLE_CHOICE', '1111,1011,1101,1001', '1111', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Digital Electronics', 'MEDIUM');

-- Question 5 (Multiple Choice, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which of the following is NOT a type of flip-flop?', 'MULTIPLE_CHOICE', 'JK Flip-Flop,SR Flip-Flop,DK Flip-Flop,D Flip-Flop', 'DK Flip-Flop', 4, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Digital Electronics', 'HARD');

-- Question 6 (TRUE_FALSE, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Digital electronics deals with discrete signals rather than continuous signals.', 'TRUE_FALSE', 'True,False', 'True', 1, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Digital Electronics', 'EASY');

-- Question 7 (TRUE_FALSE, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'In digital electronics, the hexadecimal number system has a base of 8.', 'TRUE_FALSE', 'True,False', 'False', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Digital Electronics', 'MEDIUM');

-- Question 8 (SHORT_ANSWER, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What does ASCII stand for in digital communication?', 'SHORT_ANSWER', '', 'American Standard Code for Information Interchange', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Digital Electronics', 'MEDIUM');

-- Question 9 (SHORT_ANSWER, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Explain the concept of clock skew in digital circuits.', 'SHORT_ANSWER', '', 'Clock skew is the difference in arrival time of a clock signal at different parts of a circuit', 5, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Digital Electronics', 'HARD');

-- Question 10 (MULTIPLE_ANSWER, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which of the following are types of digital-to-analog converters?', 'MULTIPLE_ANSWER', 'Binary Weighted Resistor DAC,R-2R Ladder DAC,Pulse Width Modulation DAC,Sigma-Delta DAC,Flash DAC', 'Binary Weighted Resistor DAC,R-2R Ladder DAC,Pulse Width Modulation DAC,Sigma-Delta DAC', 5, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Digital Electronics', 'HARD');

-- 2. Basic Electrical Concepts (10 questions)
-- Question 1 (Multiple Choice, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What is the unit of electrical current?', 'MULTIPLE_CHOICE', 'Volt,Ampere,Ohm,Watt', 'Ampere', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Basic Electrical Concepts', 'EASY');

-- Question 2 (Multiple Choice, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which law states that the current through a conductor is directly proportional to the voltage?', 'MULTIPLE_CHOICE', 'Ohm\'s Law,Kirchhoff\'s Law,Faraday\'s Law,Lenz\'s Law', 'Ohm\'s Law', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Basic Electrical Concepts', 'EASY');

-- Question 3 (Multiple Choice, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What is the equivalent resistance of two 10 ohm resistors connected in parallel?', 'MULTIPLE_CHOICE', '20 ohms,5 ohms,10 ohms,0 ohms', '5 ohms', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Basic Electrical Concepts', 'MEDIUM');

-- Question 4 (Multiple Choice, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which component is used to store electrical energy in an electric field?', 'MULTIPLE_CHOICE', 'Resistor,Inductor,Capacitor,Diode', 'Capacitor', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Basic Electrical Concepts', 'MEDIUM');

-- Question 5 (Multiple Choice, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'In an RLC circuit at resonance, what is the phase relationship between voltage and current?', 'MULTIPLE_CHOICE', 'Current leads voltage by 90 degrees,Voltage leads current by 90 degrees,Current and voltage are in phase,Current and voltage are 180 degrees out of phase', 'Current and voltage are in phase', 4, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Basic Electrical Concepts', 'HARD');

-- Question 6 (TRUE_FALSE, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'The unit of electrical resistance is the Watt.', 'TRUE_FALSE', 'True,False', 'False', 1, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Basic Electrical Concepts', 'EASY');

-- Question 7 (TRUE_FALSE, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'In a series circuit, the current is the same through all components.', 'TRUE_FALSE', 'True,False', 'True', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Basic Electrical Concepts', 'MEDIUM');

-- Question 8 (SHORT_ANSWER, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What is the formula for electrical power?', 'SHORT_ANSWER', '', 'P = VI', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Basic Electrical Concepts', 'MEDIUM');

-- Question 9 (SHORT_ANSWER, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Explain the difference between conductors and insulators.', 'SHORT_ANSWER', '', 'Conductors allow the free flow of electrons while insulators restrict the flow of electrons', 5, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Basic Electrical Concepts', 'HARD');

-- Question 10 (MULTIPLE_ANSWER, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which of the following are passive electrical components?', 'MULTIPLE_ANSWER', 'Resistor,Capacitor,Inductor,Transistor,Diode', 'Resistor,Capacitor,Inductor', 5, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Basic Electrical Concepts', 'HARD');

-- 3. Logic Gates (10 questions)
-- Question 1 (Multiple Choice, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which logic gate performs the AND operation?', 'MULTIPLE_CHOICE', 'OR Gate,AND Gate,NOT Gate,XOR Gate', 'AND Gate', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Gates', 'EASY');

-- Question 2 (Multiple Choice, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'The output of an OR gate is HIGH when:', 'MULTIPLE_CHOICE', 'All inputs are HIGH,At least one input is HIGH,All inputs are LOW,No inputs are HIGH', 'At least one input is HIGH', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Gates', 'EASY');

-- Question 3 (Multiple Choice, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which logic gate is known as the universal gate?', 'MULTIPLE_CHOICE', 'AND,OR,NOT,NAND', 'NAND', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Gates', 'MEDIUM');

-- Question 4 (Multiple Choice, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What is the output of an XOR gate when both inputs are HIGH?', 'MULTIPLE_CHOICE', 'HIGH,LOW,Undefined,Floating', 'LOW', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Gates', 'MEDIUM');

-- Question 5 (Multiple Choice, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which combination of gates can be used to create an XOR gate?', 'MULTIPLE_CHOICE', 'AND and OR,NAND and NOR,AND, OR, and NOT,All of the above', 'All of the above', 4, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Gates', 'HARD');

-- Question 6 (TRUE_FALSE, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'A NOT gate inverts the input signal.', 'TRUE_FALSE', 'True,False', 'True', 1, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Gates', 'EASY');

-- Question 7 (TRUE_FALSE, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'A NOR gate is equivalent to an OR gate followed by a NOT gate.', 'TRUE_FALSE', 'True,False', 'True', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Gates', 'MEDIUM');

-- Question 8 (SHORT_ANSWER, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Write the Boolean expression for a 2-input AND gate.', 'SHORT_ANSWER', '', 'Y = A·B', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Gates', 'MEDIUM');

-- Question 9 (SHORT_ANSWER, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Explain De Morgan\'s theorem and its application in digital logic.', 'SHORT_ANSWER', '', 'De Morgan\'s theorem states that NOT(A AND B) = NOT A OR NOT B and NOT(A OR B) = NOT A AND NOT B', 5, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Gates', 'HARD');

-- Question 10 (MULTIPLE_ANSWER, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which of the following are universal gates?', 'MULTIPLE_ANSWER', 'AND,OR,NOT,NAND,NOR', 'NAND,NOR', 5, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Gates', 'HARD');

-- 4. Semiconductor Devices (10 questions)
-- Question 1 (Multiple Choice, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which of the following is a semiconductor material?', 'MULTIPLE_CHOICE', 'Copper,Silicon,Gold,Iron', 'Silicon', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Semiconductor Devices', 'EASY');

-- Question 2 (Multiple Choice, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What type of semiconductor is created by adding impurities to increase the number of free electrons?', 'MULTIPLE_CHOICE', 'P-type,N-type,PN-type,Intrinsic', 'N-type', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Semiconductor Devices', 'EASY');

-- Question 3 (Multiple Choice, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which semiconductor device is used for amplification?', 'MULTIPLE_CHOICE', 'Diode,Transistor,Resistor,Capacitor', 'Transistor', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Semiconductor Devices', 'MEDIUM');

-- Question 4 (Multiple Choice, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What is the main function of a diode?', 'MULTIPLE_CHOICE', 'Amplification,Rectification,Oscillation,Modulation', 'Rectification', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Semiconductor Devices', 'MEDIUM');

-- Question 5 (Multiple Choice, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which of the following is NOT a type of transistor?', 'MULTIPLE_CHOICE', 'BJT,JFET,MOSFET,DIODE', 'DIODE', 4, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Semiconductor Devices', 'HARD');

-- Question 6 (TRUE_FALSE, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'A diode allows current to flow in both directions.', 'TRUE_FALSE', 'True,False', 'False', 1, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Semiconductor Devices', 'EASY');

-- Question 7 (TRUE_FALSE, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'The depletion region in a PN junction increases when reverse biased.', 'TRUE_FALSE', 'True,False', 'True', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Semiconductor Devices', 'MEDIUM');

-- Question 8 (SHORT_ANSWER, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What are the three terminals of a BJT transistor?', 'SHORT_ANSWER', '', 'Emitter, Base, Collector', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Semiconductor Devices', 'MEDIUM');

-- Question 9 (SHORT_ANSWER, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Explain the working principle of a Zener diode.', 'SHORT_ANSWER', '', 'A Zener diode operates in the reverse breakdown region and maintains a constant voltage across its terminals', 5, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Semiconductor Devices', 'HARD');

-- Question 10 (MULTIPLE_ANSWER, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which of the following are types of special purpose diodes?', 'MULTIPLE_ANSWER', 'Zener Diode,LED,Photodiode,Schottky Diode,Varactor Diode', 'Zener Diode,LED,Photodiode,Schottky Diode,Varactor Diode', 5, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Semiconductor Devices', 'HARD');

-- 5. Logic Design & Combinational Circuits (10 questions)
-- Question 1 (Multiple Choice, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What is a multiplexer?', 'MULTIPLE_CHOICE', 'A device that selects one of many inputs and forwards it to the output,A device that distributes one input to many outputs,A device that adds binary numbers,A device that stores binary data', 'A device that selects one of many inputs and forwards it to the output', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Design & Combinational Circuits', 'EASY');

-- Question 2 (Multiple Choice, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'How many select lines are required for an 8-to-1 multiplexer?', 'MULTIPLE_CHOICE', '2,3,4,8', '3', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Design & Combinational Circuits', 'EASY');

-- Question 3 (Multiple Choice, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What is a half adder used for?', 'MULTIPLE_CHOICE', 'Adding two binary digits,Adding three binary digits,Subtracting two binary digits,Multiplying two binary digits', 'Adding two binary digits', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Design & Combinational Circuits', 'MEDIUM');

-- Question 4 (Multiple Choice, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which of the following is NOT a combinational circuit?', 'MULTIPLE_CHOICE', 'Multiplexer,Demultiplexer,Flip-flop,Encoder', 'Flip-flop', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Design & Combinational Circuits', 'MEDIUM');

-- Question 5 (Multiple Choice, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What is the main difference between a decoder and an encoder?', 'MULTIPLE_CHOICE', 'A decoder has more inputs than outputs while an encoder has more outputs than inputs,A decoder has more outputs than inputs while an encoder has more inputs than outputs,A decoder works with analog signals while an encoder works with digital signals,There is no difference', 'A decoder has more outputs than inputs while an encoder has more inputs than outputs', 4, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Design & Combinational Circuits', 'HARD');

-- Question 6 (TRUE_FALSE, EASY)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'A full adder can add three binary digits.', 'TRUE_FALSE', 'True,False', 'True', 1, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Design & Combinational Circuits', 'EASY');

-- Question 7 (TRUE_FALSE, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Combinational circuits have memory elements.', 'TRUE_FALSE', 'True,False', 'False', 2, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Design & Combinational Circuits', 'MEDIUM');

-- Question 8 (SHORT_ANSWER, MEDIUM)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'What are the outputs of a half adder?', 'SHORT_ANSWER', '', 'Sum and Carry', 3, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Design & Combinational Circuits', 'MEDIUM');

-- Question 9 (SHORT_ANSWER, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Explain the concept of a Karnaugh map and its purpose in logic design.', 'SHORT_ANSWER', '', 'A Karnaugh map is a graphical method used for simplifying Boolean algebra expressions', 5, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Design & Combinational Circuits', 'HARD');

-- Question 10 (MULTIPLE_ANSWER, HARD)
INSERT INTO questions (question_id, question_text, question_type, options, correct_answers, marks, unit_code, created_by, approved, topic, difficulty)
VALUES (UUID(), 'Which of the following are examples of combinational circuits?', 'MULTIPLE_ANSWER', 'Multiplexer,Demultiplexer,Encoder,Decoder,Flip-flop', 'Multiplexer,Demultiplexer,Encoder,Decoder', 5, 'SCO100', 'e2b78635-a080-46cc-81a8-69025e337415', 1, 'Logic Design & Combinational Circuits', 'HARD');
