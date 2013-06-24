'''
This code is for producing seeds for URLS to be 
crawled via the index pages.
'''

import argparse
from heritrix import seeds_report

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('seeds_report', metavar = 'seeds-report')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	for report_code, crawled_or_not, seed in seeds_report.parse_report(parsed.seeds_report):
		if report_code == seeds_report.ROBOTS_TXT_BLOCKED_ERR_CODE:
			def fix_seed(seed):
				return seed.replace('2012/', '')

			print fix_seed(seed)