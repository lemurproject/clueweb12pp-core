'''
Heritrix code to parse the seeds report
'''

from collections import namedtuple


ROBOTS_TXT_BLOCKED_ERR_CODE = '-9998'

SeedReport = namedtuple(
	'SeedReport', 
	['report_code', 'crawled_or_not', 'seed']
)

def parse_report(seeds_report):
	with open(seeds_report, 'r') as seeds_report_handle:
		_ = seeds_report_handle.readline()  # ignore header

		for line in seeds_report_handle:
			report, crawled_or_not, seed = line.strip().split()

			yield SeedReport(report, crawled_or_not, seed)

