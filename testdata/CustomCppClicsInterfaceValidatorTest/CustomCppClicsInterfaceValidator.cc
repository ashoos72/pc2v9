/* Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau. */
#include <fstream>
#include <iostream>
#include <string>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cassert>
#include <cmath>
#include <cstdarg>

const int EXIT_AC = 42;
const int EXIT_WA = 43;

std::ifstream judgein, judgeans;
FILE *judgemessage = NULL;

void wrong_answer(const char *err, ...) {
	va_list pvar;
	va_start(pvar, err);
	vfprintf(judgemessage, err, pvar);
	fprintf(judgemessage, "\n");
	exit(EXIT_WA);
}

void judge_error(const char *err, ...) {
	va_list pvar;
	va_start(pvar, err);
	// If judgemessage hasn't been set up yet, write error to stderr
	if (!judgemessage) judgemessage = stderr;
	vfprintf(judgemessage, err, pvar);
	fprintf(judgemessage, "\n");
	assert(!"Judge Error");
}

bool isfloat(const char *s, double &val) {
	char trash[20];
	double v;
	if (sscanf(s, "%lf%10s", &v, trash) != 1) return false;
	val = v;
	return true;
}

template <typename Stream>
void openfile(Stream &stream, const char *file, const char *whoami) {
	stream.open(file);
	if (stream.fail()) {
		judge_error("%s: failed to open %s\n", whoami, file);
	}
}

FILE *openfeedback(const char *feedbackdir, const char *feedback, const char *whoami) {
	char path[10000];
	sprintf(path, "%s%s", feedbackdir, feedback);
	FILE *res = fopen(path, "w");
	if (!res) {
		judge_error("%s: failed to open %s for writing", whoami, path);
	}
	return res;
}

const char *USAGE = "Usage: %s judge_in judge_ans feedback_file [options] < team_out";

int main(int argc, char **argv) {
	if(argc < 4) {
		judge_error(USAGE, argv[0]);
	}
	judgemessage = openfeedback(argv[3], "judgemessage.txt", argv[0]);
	openfile(judgein, argv[1], argv[0]);
	openfile(judgeans, argv[2], argv[0]);

	bool case_sensitive = false;
	bool space_change_sensitive = false;
	bool use_floats = false;
	double float_abs_tol = -1;
	double float_rel_tol = -1;

	for (int a = 4; a < argc; ++a) {
		if        (!strcmp(argv[a], "case_sensitive")) {
			case_sensitive = true;
		} else if (!strcmp(argv[a], "space_change_sensitive")) {
			space_change_sensitive = true;
		} else if (!strcmp(argv[a], "float_absolute_tolerance")) {
			if (a+1 == argc || !isfloat(argv[a+1], float_abs_tol))
				judge_error(USAGE, argv[0]);
			++a;
		} else if (!strcmp(argv[a], "float_relative_tolerance")) {
			if (a+1 == argc || !isfloat(argv[a+1], float_rel_tol))
				judge_error(USAGE, argv[0]);
			++a;
		} else if (!strcmp(argv[a], "float_tolerance")) {
			if (a+1 == argc || !isfloat(argv[a+1], float_rel_tol))
				judge_error(USAGE, argv[0]);
			float_abs_tol = float_rel_tol;
			++a;
		} else {
			judge_error(USAGE, argv[0]);
		}
	}
	use_floats = float_abs_tol >= 0 || float_rel_tol >= 0;
   
	std::string judge, team;
	char trash[20];
	while (true) {
		if (space_change_sensitive) {
			while (isspace(judgeans.peek())) {
				char c = judgeans.get();
				char d = std::cin.get();
				if (c != d) {
					wrong_answer("Space change error: got %d expected %d", d, c);
				}
			}
			if (isspace(std::cin.peek())) {
				wrong_answer("Space change error: judge out of space, got %d from team", std::cin.get());
			}
		}
		if (!(judgeans >> judge))
			break;
		if (!(std::cin >> team)) {
			wrong_answer("User EOF while judge had more output\n(Next judge token: %s)", judge.c_str());
		}
     
		double jval, tval;
		if (use_floats && isfloat(judge.c_str(), jval)) {
			if (!isfloat(team.c_str(), tval)) {
				wrong_answer("Expected float, got: %s", team.c_str());
			}
			if(!(fabs(jval - tval) <= float_abs_tol) && 
			   !(fabs(jval - tval) <= float_rel_tol*fabs(jval))) {
				wrong_answer("Too large difference.\n Judge: %lf\n Team: %lf\n Difference: %lf\n (abs tol %lf rel tol %lf)", 
							 jval, tval, jval-tval, float_abs_tol, float_rel_tol);
			}
		} else if (case_sensitive) {
			if (strcmp(judge.c_str(), team.c_str()) != 0) {
				wrong_answer("String tokens mismatch\nJudge: \"%s\"\nTeam: \"%s\"\n", judge.c_str(), team.c_str());
			}
		} else {
			if(strcasecmp(judge.c_str(), team.c_str()) != 0) {
				wrong_answer("String tokens mismatch\nJudge: \"%s\"\nTeam: \"%s\"\n", judge.c_str(), team.c_str());
			}
		}
	}

	if (std::cin >> team) {
		wrong_answer("Trailing output");
	}

	fprintf(judgemessage, "Correct output\n");
	exit(EXIT_AC);
}
