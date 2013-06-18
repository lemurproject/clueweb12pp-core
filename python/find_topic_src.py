'''
the nabble dataset has a -td style topic link which 
are redirects from -tp style topic links found in the index pages.
We going to unravel and find the tp links first and then the pages next
'''
import argparse
import sys
from heritrix import crawl_log

sys.stderr = open('nabble_topic_fails.txt', 'w+')

def process_job_directories(job_directories):
	final_target_src_map = {}

	for job_directory in job_directories:
		target_src_map = crawl_log.process_heritrix_job(job_directory, redirect = True)
		final_target_src_map = dict(final_target_src_map.items() + target_src_map.items())

	return final_target_src_map

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('topic_links', metavar = 'topic-links', help = 'List of topic links')
		parser.add_argument('job_directories', metavar = 'job-directories', nargs = '+', help = 'List of nabble post directories')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	target_src_map = process_job_directories(parsed.job_directories)

	with open(parsed.topic_links, 'r') as topic_links_handle:
		for link in topic_links_handle:
			if link.strip() not in target_src_map:
				sys.stderr.write(link.strip() + '\n')
				continue
			print target_src_map[link.strip()]
			sys.stdout.flush()