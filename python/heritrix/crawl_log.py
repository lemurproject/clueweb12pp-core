'''
heritrix job crawl log processor
'''

import argparse
import os



def process_heritrix_job(heritrix_job_directory, redirect = False):
	final_target_src_map = {}
	for crawl_log_file in get_heritrix_files(heritrix_job_directory):
		if not redirect:
			target_src_map = parse_crawl_log_file(crawl_log_file)
			final_target_src_map = dict(final_target_src_map.items() + target_src_map.items())
		else:
			target_src_map = parse_crawl_log_file_redirects(crawl_log_file)
			final_target_src_map = dict(final_target_src_map.items() + target_src_map.items())

	return final_target_src_map

def process_heritrix_job_links(heritrix_job_directory):
	for crawl_log_file in get_heritrix_files(heritrix_job_directory):
		for link in parse_crawl_log_file_raw(crawl_log_file):
			print link

def get_heritrix_files(heritrix_job):
	'''
	Gets crawler log file sequence for the job directory
	'''
	for root, dirs, files in os.walk(heritrix_job):
		for filename in files:
			if filename.find('crawl.log') >= 0 and filename.find('lck') < 0:
				yield os.path.join(root, filename)

def parse_crawl_log_file_raw(crawl_log_file):
	'''
	Read only the downloaded links from the crawl log
	'''
	with open(crawl_log_file, 'r') as crawl_log_file_handle:
		for new_line in crawl_log_file_handle:
			_, _, _, target, _, _, _, _, _, _, _, _ = new_line.split()

			yield target

def parse_crawl_log_file(crawl_log_file):
	'''
	Harvest the link graph from the crawl log
	'''
	target_src_map = {}

	ignore_extensions = ['.js', '.css', '.ico', '.xml', 'rdf']

	with open(crawl_log_file, 'r') as crawl_log_file_handle:
		for new_line in crawl_log_file_handle:
			_, _, _, target, _, src, content_type, _, _, _, _, _ = new_line.split()

			if content_type.find('text/dns') >= 0:
				continue

			if target.find('robots.txt') >= 0:
				continue

			if any([target.endswith(ext) for ext in ignore_extensions]):
				continue


			if src.find('-') >= 0:
				continue

			target_src_map[target] = src

	return target_src_map

def parse_crawl_log_file_redirects(crawl_log_file):
	target_src_map = {}

	with open(crawl_log_file, 'r') as crawl_log_file_handle:
		for new_line in crawl_log_file_handle:

			_, _, _, target, http_op, src, content_type, _, _, _, _, _ = new_line.split()

			if target.find('robots.txt') >= 0:
				continue

			if http_op != 'R':
				continue

			if src == '-':
				continue

			target_src_map[target] = src

	return target_src_map

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('heritrix_job_directory', metavar = 'heritrix-job-directory', help = '/path/to/heritrix-job-directory')
		parser.add_argument('-g', '--graph', action = 'store_true', default = False, help = 'Graph as opposed to just the links', dest = 'graph')
		parser.add_argument('-r', '--redirects', action = 'store_true', default = False, help = 'Get only the redirect graph', dest = 'redirects')

		return parser.parse_args()

	parsed = parse_cmdline_args()

	target_src_map = None
	if parsed.graph:
		if not parsed.redirects:
			target_src_map = process_heritrix_job(parsed.heritrix_job_directory)
		else:
			target_src_map = process_heritrix_job(parsed.heritrix_job_directory, redirect = True)
	else:
		process_heritrix_job_links(parsed.heritrix_job_directory)
