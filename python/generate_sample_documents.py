import argparse
import clueweb12pp_core


if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('warc_file', metavar = 'warc-file')
		parser.add_argument('sample_prefix', metavar = 'sample-prefix')

		return parser.parse_args()

	parsed = parse_cmdline_args()

clueweb12pp_core.sample_documents(parsed.warc_file, parsed.sample_prefix, 2)